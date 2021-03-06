package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;
/**
 *
 * Parts of this code are taken from Riyad Kalla written wrapper
 * {@link http://www.thebuzzmedia.com/software/exiftool-enhanced-java-integration-for-exiftool/}
 * and are provided via the following license:
 *
 * Copyright 2011 The Buzz Media, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.io.ByteStreams;
import com.google.gson.*;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagGroupBuilder;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class ExifToolWrapper
{
    private static Logger logger = LoggerFactory.getLogger(ExifToolWrapper.class);

    /**
     * Environment variable referring to exiftool call
     */
    public static final String ENVIRONMENT_VAR_EXIFTOOL_PATH = "EXIFTOOL";

    /**
     * java system property used to lookup for the exiftool program
     */
    public static final String JAVA_SYSTEM_PROPERTY = "exiftool.path";

    private static String pathToExifTool;

    static
    {
        if (System.getenv().containsKey(ENVIRONMENT_VAR_EXIFTOOL_PATH))
        {
            pathToExifTool = System.getenv().get(ENVIRONMENT_VAR_EXIFTOOL_PATH);
        }
        else
        {
          pathToExifTool = System.getProperty(JAVA_SYSTEM_PROPERTY,"exiftool");
        }
    }

    private Map<MetadataEncodingScope, String> characterEncoding;

    public static String getPathToExifTool() {
        return pathToExifTool;
    }

    public static void setPathToExifTool( final String pathToExifTool )
    {
        ExifToolWrapper.pathToExifTool = pathToExifTool;
    }

    public static final long PROCESS_CLEANUP_DELAY = Long.getLong(
            "exiftool.processCleanupDelay", 600000);

    /**
     * Name used to identify the (optional) cleanup {@link Thread}.
     * <p/>
     * This is only provided to make debugging and profiling easier for
     * implementors making use of this class such that the resources this class
     * creates and uses (i.e. Threads) are readily identifiable in a running VM.
     * <p/>
     * Default value is "<code>ExifTool Cleanup Thread</code>".
     */
    protected static final String CLEANUP_THREAD_NAME = "ExifTool Cleanup Thread";

    /**
     * @param args Should only contain commandline parameters which have to be passed to the tool
     */
    protected static IOStream startExifToolProcess(List<String> args)
            throws RuntimeException, ExifToolIntegrationException
    {
        Process proc;
        IOStream streams;

        logger.debug("\tAttempting to start external ExifTool process using args: %s",
                args);

        args.add(0, getPathToExifTool());
        try
        {
            proc = new ProcessBuilder(args).start();
            logger.debug("\t\tSuccessful");
        }
        catch (Throwable t)
        {
            List<String> cmdArgs = new ArrayList<>(args);
            cmdArgs.remove(0);
            throw new ExifToolIntegrationException("Call of exiftool at \"" + args.get(0) + "\" failed. Commandline args: " + cmdArgs, t);
        }

        logger.debug("\tSetting up Read/Write streams to the external ExifTool process...");

        // Setup read/write streams to the new process.
        streams = new IOStream(new BufferedReader(new InputStreamReader(
                proc.getInputStream())), new OutputStreamWriter(
                proc.getOutputStream()));

        logger.debug("\t\tSuccessful, returning streams to caller.");
        return streams;
    }

    public void setCharacterEncoding(final Map<MetadataEncodingScope,String> characterEncoding)
    {
        this.characterEncoding = characterEncoding;
    }

    /**
     * Simple class used to house the read/write streams used to communicate
     * with an external ExifTool process as well as the logic used to safely
     * close the streams when no longer needed.
     * <p/>
     * This class is just a convenient way to group and manage the read/write
     * streams as opposed to making them dangling member variables off of
     * ExifTool directly.
     *
     * @author Riyad Kalla (software@thebuzzmedia.com)
     * @since 1.1
     */
    private static class IOStream
    {
        BufferedReader reader;
        OutputStreamWriter writer;

        public IOStream(BufferedReader reader, OutputStreamWriter writer)
        {
            this.reader = reader;
            this.writer = writer;
        }

        public void close()
        {
            try
            {
                logger.debug("\tClosing Read stream...");
                reader.close();
                logger.debug("\t\tSuccessful");
            }
            catch (Exception e)
            {
                // no-op, just try to close it.
            }

            try
            {
                logger.debug("\tClosing Write stream...");
                writer.close();
                logger.debug("\t\tSuccessful");
            }
            catch (Exception e)
            {
                // no-op, just try to close it.
            }

            // Null the stream references.
            reader = null;
            writer = null;

            logger.debug("\tRead/Write streams successfully closed.");
        }
    }

    private Timer cleanupTimer;
    private TimerTask currentCleanupTask;

    private IOStream streams;

    /**
     * if true leaves the exiftool process open for subsequent calls.
     */
    private boolean stayOpen = false;

    private ExifToolWrapper(boolean stayOpen)
    {
        this.stayOpen = stayOpen;

        if (stayOpen && PROCESS_CLEANUP_DELAY > 0)
        {
            this.cleanupTimer = new Timer(CLEANUP_THREAD_NAME, true);

            // Start the first cleanup task counting down.
            resetCleanupTask();
        }
    }

    /**
     * Used to shutdown the external ExifTool process and close the read/write
     * streams used to communicate with it when {@link #stayOpen} is
     * enabled.
     * <p/>
     * <strong>NOTE</strong>: Calling this method does not preclude this
     * instance of {@link ExifToolWrapper} from being re-used, it merely disposes of
     * the native and internal resources until the next call to
     * <code>getImageMeta</code> causes them to be re-instantiated.
     * <p/>
     * The cleanup thread will automatically call this after an interval of
     * inactivity defined by {@link #PROCESS_CLEANUP_DELAY}.
     * <p/>
     * Calling this method on an instance of this class without
     * {@link #stayOpen} support enabled has no effect.
     */
    public void close()
    {
        /*
         * no-op if the underlying process and streams have already been closed
		 * OR if stayOpen was never used in the first place in which case
		 * nothing is open right now anyway.
		 */
        if (streams == null)
            return;

        try
        {
            logger.debug("\tAttempting to close ExifTool daemon process, issuing '-stay_open\\nFalse\\n' command...");

            // Tell the ExifTool process to exit.
            streams.writer.write("-stay_open\nFalse\n");
            streams.writer.flush();

            logger.debug("\t\tSuccessful");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            streams.close();
        }

        streams = null;
        logger.debug("\tExifTool daemon process successfully terminated.");
    }

    protected String runExiftool(final File imageFile, final List<String> cmdArgs) throws ExifToolIntegrationException
    {
        return runExiftool(imageFile, cmdArgs.toArray(new String[cmdArgs.size()]));
    }

    /**
     *
     * @param givenArguments arguments given by callee
     * @return callee's argument merged with arguments given by builder
     */
    private List<String> addConfiguredArguments( final String ... givenArguments )
    {
        final List<String> toReturn = new ArrayList(Arrays.asList(givenArguments));

        if( characterEncoding!= null && characterEncoding.size() > 0 )
        {
            for (MetadataEncodingScope metadataFormat : characterEncoding.keySet())
            {
                toReturn.add( "-charset");
                toReturn.add( metadataFormat + "=" + characterEncoding.get(metadataFormat));
            }
        }
        return toReturn;
    }

    /**
     * @param imageFile may be null. This is usefull in order to gather settings from exiftool, like e.g. "exiftool - charset" which is
     *                  used to get the list of supported charsets.
     */
    protected synchronized String runExiftool(final File imageFile, final String... cmdArgs) throws ExifToolIntegrationException
    {
        final List<String> mergedArgs = addConfiguredArguments(cmdArgs);


        StringBuilder sb = new StringBuilder();
        try
        {
            long exifToolCallElapsedTime;
            if (stayOpen)
            {
                logger.debug("\tUsing ExifTool in daemon mode (-stay_open True)...");

                // Always reset the cleanup task.
                resetCleanupTask();

			/*
             * If this is our first time calling getImageMeta with a stayOpen
			 * connection, set up the persistent process and run it so it is
			 * ready to receive commands from us.
			 */
                if (streams == null)
                {
                    final List<String> args = new ArrayList<>();
                    logger.debug("\tStarting daemon ExifTool process and creating read/write streams (this only happens once)...");

                    args.add("-stay_open");
                    args.add("True");
                    args.add("-@");
                    args.add("-");

                    // Begin the persistent ExifTool process.
                    streams = startExifToolProcess(args);
                }

                logger.debug("\tStreaming arguments to ExifTool process...");

                for (String cmdArg : mergedArgs)
                {
                    streams.writer.write(cmdArg + "\n");
                }

                if (imageFile != null)
                {
                    streams.writer.write(imageFile.getAbsolutePath());
                    streams.writer.write('\n');
                }
                logger.debug("\tExecuting ExifTool...");
                logger.debug("Using arguments: " + mergedArgs);
                // Begin tracking the duration ExifTool takes to respond.
                exifToolCallElapsedTime = System.currentTimeMillis();

                // Begin tracking the duration ExifTool takes to respond.
                // Run ExifTool on our file with all the given arguments.
                streams.writer.write("-execute\n");
                streams.writer.flush();
            }
            else
            {
                logger.debug("\tUsing ExifTool in non-daemon mode (-stay_open False)...");

			/*
			 * Since we are not using a stayOpen process, we need to setup the
			 * execution arguments completely each time.
			 */
                final List<String> args = new ArrayList<>();
                args.addAll(mergedArgs);

                if (imageFile != null)
                {
                    args.add(imageFile.getAbsolutePath());
                }

                logger.debug("\tExecuting ExifTool...");
                exifToolCallElapsedTime = System.currentTimeMillis();
                // Run the ExifTool with our args.
                logger.debug("Using arguments: " + args);
                streams = startExifToolProcess(args);

            }

            logger.debug("\tReading response back from ExifTool...");

            String line;

            while ((line = streams.reader.readLine()) != null)
            {
			/*
			 * When using a persistent ExifTool process, it terminates its
			 * output to us with a "{ready}" clause on a new line, we need to
			 * look for it and break from this loop when we see it otherwise
			 * this process will hang indefinitely blocking on the input stream
			 * with no data to read.
			 */
                sb.append(line);
                if (stayOpen && line.equals("{ready}"))
                    break;
            }

            logger.debug("\tFinished reading ExifTool response in %d ms.",
                    (System.currentTimeMillis() - exifToolCallElapsedTime));
        }
        catch (IOException e)
        {
            throw new ExifToolIntegrationException(e);
        }
        finally
        {
            if (!stayOpen && streams != null)
            {
                /*
		 * If we are not using a persistent ExifTool process, then after running
		 * the command above, the process exited in which case we need to clean
		 * our streams up since it no longer exists. If we were using a
		 * persistent ExifTool process, leave the streams open for future calls.
		 */
                streams.close();
                streams = null;
            }
        }
        return sb.toString();
    }

    /**
     * Helper method used to make canceling the current task and scheduling a
     * new one easier.
     * <p/>
     * It is annoying that we cannot just reset the timer on the task, but that
     * isn't the way the java.util.Timer class was designed unfortunately.
     */
    private void resetCleanupTask()
    {
        // no-op if the timer was never created.
        if (cleanupTimer == null)
            return;

        logger.debug("\tResetting cleanup task...");

        // Cancel the current cleanup task if necessary.
        if (currentCleanupTask != null)
            currentCleanupTask.cancel();

        // Schedule a new cleanup task.
        cleanupTimer.schedule(
                (currentCleanupTask = new CleanupTimerTask(this)),
                PROCESS_CLEANUP_DELAY, PROCESS_CLEANUP_DELAY);

        logger.debug("\t\tSuccessful");
    }

    /**
     * Class used to represent the {@link TimerTask} used by the internal auto
     * cleanup {@link Timer} to call {@link ExifToolWrapper#close()} after a specified
     * interval of inactivity.
     *
     * @author Riyad Kalla (software@thebuzzmedia.com)
     * @since 1.1
     */
    private class CleanupTimerTask extends TimerTask
    {
        private ExifToolWrapper owner;

        public CleanupTimerTask(ExifToolWrapper owner) throws IllegalArgumentException
        {
            if (owner == null)
                throw new IllegalArgumentException(
                        "owner cannot be null and must refer to the ExifTool instance creating this task.");

            this.owner = owner;
        }

        @Override
        public void run()
        {
            logger.debug("\tAuto cleanup task running...");
            owner.close();
        }
    }

    /**
     * The core function used to set image metadata. It expects a multimap where each key points to a list of values.
     * The key has to be a fully qualified name consisting of group name and tag id. E.g.:
     * <pre>
     *     IPTC:Keywords
     * </pre>
     * For each item found in the value list the method adds the following argument to the resulting command line call:
     * <pre>
     *     -KEY=VALUE
     * </pre>
     * Having an entry like
     * <pre>
     *     "IPTC:Keywords" --> {"politics", "government"}
     * </pre>
     * results in
     * <pre>
     *     -IPTC:Keywords=politics -IPTC:Keywords=government
     * </pre>
     * <p/>
     * The value string may contain a JSON format which can be used to set up complex XMP entries.
     *
     * @param image   image to modify
     * @param tags    Map<String, List<String>> which specifies single/multiple values for a given tag (key)
     * @param options additional commandline parameters. See also {@link ExifToolWrapper.ExifToolOptionBuilder}. May be null
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IOException
     * @throws ExifToolIntegrationException
     */
    public void setImageMeta(File image, ListMultimap<String, String> tags, List<String> options)
            throws IllegalArgumentException, SecurityException, IOException, ExifToolIntegrationException
    {
        if (image == null)
            throw new IllegalArgumentException(
                    "image cannot be null and must be a valid stream of image data.");
        if (tags == null || tags.size() == 0)
            throw new IllegalArgumentException(
                    "tags cannot be null and must contain 1 or more Tag to query the image for.");
        if (!image.canWrite())
            throw new SecurityException(
                    "Unable to read the given image ["
                            + image.getAbsolutePath()
                            + "], ensure that the image exists at the given path and that the executing Java process has permissions to read it.");

        logger.debug("Writing %d tags to image: %s", tags.size(),
                image.getAbsolutePath());

        final List<String> cmdArgs;
        if (options != null)
        {
            cmdArgs = new ArrayList<>(options);
        }
        else
        {
            cmdArgs = new ArrayList<>();
        }

        addArgsToSetImageMetadata(cmdArgs, tags);
        runExiftool(image, cmdArgs);
    }

    private void addArgsToSetImageMetadata(final List<String> args, final ListMultimap<String, String> tags)
    {
        for (Map.Entry<String, String> entry : tags.entries())
        {
            args.add("-" + entry.getKey() + "=" + entry.getValue());
        }
    }

    /**
     * Wrapper of the call
     * <pre>
     *     exiftool -listx -S
     * </pre>
     * which gives an overview of the supported tag groups and the tags which each group provides.
     *
     * @return group names supported by exiftool.
     * @throws ExifToolIntegrationException
     */
    public TagInfo getSupportedTagsOfGroups()
            throws ExifToolIntegrationException
    {
        String etResult = runExiftool(null, "-listx", "-S");

        final TagInfo toReturn;
        try
        {
            toReturn = parseTagInfoFromXMLInput(etResult);
        }
        catch (ParserConfigurationException | SAXException e)
        {
            throw new ExifToolIntegrationException("DOM parser setup DOM failed", e);
        }
        catch (IOException e)
        {
            throw new ExifToolIntegrationException(e);
        }
        return toReturn;
    }

    /**
     * @deprecated
     * @param nameOfTagGroups the list of tag groups to inspect. An entry may contain the specific location too. It has to be
     *                        separated by a colon. Example entries: "IPTC", "XMP:XMP-dc"
     * @return map containing tags as keys and tag values
     */
    public List readTagGroup( File image, final String ... nameOfTagGroups ) throws ExifToolIntegrationException
    {
        final List tagToValue;

        List<String> args = new ArrayList<>();
        args.add("-j");
        for (String nameOfTagGroup : nameOfTagGroups)
        {
            args.add( "-" + nameOfTagGroup);
        }

        String result = runExiftool(image, args);
        if(!Strings.isNullOrEmpty(result))
        {
            tagToValue = new Gson().fromJson( result, ArrayList.class);
        }
        else
        {
            tagToValue = new ArrayList<>();
        }
        return tagToValue;
    }

    /**
     *
     * @return json object where each field has name "GroupName:Entry" and related value. In some cases the related value can by of type
     * array.
     * Example output:
     * <pre>
      {
        "IPTC:City": "Stuttgart",
        "IPTC:CopyrightNotice": "dpa",
        "XMP:ToneCurveBlue": [
            "0, 0",
            "255, 255"
        ]
      }
     * </pre>
     * Return value may be null in case no entries could be found
     */
    public JsonObject readTagGroups( File image, final String ... nameOfTagGroups ) throws ExifToolIntegrationException
    {
        JsonObject toReturn = null;
        final List<String> args = new ArrayList();
        args.add("-j");
        for (String nameOfTagGroup : nameOfTagGroups)
        {
            args.add( "-" + nameOfTagGroup);
        }

        String result = runExiftool(image, args);
        if(!Strings.isNullOrEmpty(result))
        {
            final JsonArray array = new JsonParser().parse(result).getAsJsonArray();
            if(array.size()>0)
            {
                toReturn = array.get(0).getAsJsonObject();
            }
        }

        return toReturn;
    }

    private TagInfo parseTagInfoFromXMLInput(final String xmlInput) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(xmlInput.getBytes()));

        TagInfo tagInfo = new TagInfo();
        /*
         * read all tables
         */
        NodeList tableNodes = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < tableNodes.getLength(); i++)
        {
            Node table = tableNodes.item(i);
            NamedNodeMap attributes = table.getAttributes();
            TagGroupBuilder tagGroupBuilder = TagGroupBuilder.aTagGroup()
                    .withName(attributes.getNamedItem("name").getTextContent())
                    .withInformationType(attributes.getNamedItem("g0").getTextContent())
                    .withSpecificLocation(attributes.getNamedItem("g1").getTextContent());

            NodeList tagNodes = table.getChildNodes();
            for (int j = 0; j < tagNodes.getLength(); j++)
            {
                Node tag = tagNodes.item(j);
                if (tag.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }

                NamedNodeMap tagAttributes = tag.getAttributes();
                tagGroupBuilder.addTagGroupItem(tagAttributes.getNamedItem("id").getTextContent(),
                        tagAttributes.getNamedItem("name").getTextContent(),
                        tagAttributes.getNamedItem("type").getTextContent(),
                        Boolean.parseBoolean(tagAttributes.getNamedItem("writable").getTextContent()));
            }
            tagInfo.add(tagGroupBuilder.build());
        }

        return tagInfo;
    }

    public static ExifToolOptionBuilder exiftoolOptions()
    {
        return new ExifToolOptionBuilder();
    }

    /**
     * @deprecated Use {@link ExifTool}
     */
    public static class ExifToolOptionBuilder
    {
        List<String> options = new ArrayList<>();

        public ExifToolOptionBuilder useEncodingCharsetForIPTC(final ExifTool.CodedCharset codedCharset)
        {
            options.add("-IPTC:codedcharacterset=" + codedCharset.getCodepageId());
            return this;
        }

        public List<String> build()
        {
            return options;
        }
    }

    public static ExifToolBuilder anExifTool()
    {
        return new ExifToolBuilder();
    }


    public enum MetadataEncodingScope {
        /**
         * Set encoding for all metadata formats
         */
        ALL_FORMATS("ExifTool"),
        /**
         * Specify encoding for iptc
         */
        IPTC("IPTC"),
        /**
         * Specify encoding for exif
         */
        EXIF("EXIF")
        ;

        MetadataEncodingScope(final String scopeID)
        {
            this.encodingScope = scopeID;
        }


        private String encodingScope;

        public String getEncodingScope()
        {
            return encodingScope;
        }

        @Override public String toString()
        {
            return encodingScope;
        }
    }
    public static class ExifToolBuilder
    {
        private boolean stayOpen = false;
        private Map<MetadataEncodingScope,String> metadataTypeToEncoding = new HashMap<>();

        /**
         * leave the exiftool process open for subsequent calls.
         * If used then caller have to make sure to call {@link #close()} to shutdown the process
         */
        public ExifToolBuilder WithStayOpenMode()
        {
            stayOpen = true;
            return this;
        }

        public ExifToolBuilder withEncodingCharSet(final MetadataEncodingScope encodingScope, final String encodingCharSet)
        {
            metadataTypeToEncoding.put(encodingScope,encodingCharSet);
            return this;
        }

        public ExifToolWrapper build()
        {
            ExifToolWrapper toReturn = new ExifToolWrapper(stayOpen);
            if (metadataTypeToEncoding.size() > 0)
            {
                toReturn.setCharacterEncoding(metadataTypeToEncoding);
            }
            return toReturn;
        }
    }
}
