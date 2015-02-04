package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo;

/**
 * @author oliver langer
 */
public class TagGroupItem
{
    private final String id;
    private final String name;
    private final String type;
    private final boolean isWritable;

    protected TagGroupItem(final String id, final String name, final String type, final boolean isWritable)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isWritable = isWritable;
    }

    public String getId()
    {
        return id;
    }

    public boolean isWritable()
    {
        return isWritable;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof TagGroupItem))
            return false;

        final TagGroupItem that = (TagGroupItem) o;

        if (isWritable != that.isWritable)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (isWritable ? 1 : 0);
        return result;
    }

    @Override public String toString()
    {
        return "TagGroupItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isWritable=" + isWritable +
                '}';
    }
}
