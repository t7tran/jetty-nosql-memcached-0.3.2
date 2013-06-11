package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractSerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;

import com.thoughtworks.xstream.XStream;

public class XStreamTranscoder extends AbstractSerializationTranscoder
{
    private XStream xstream = null;
    private ClassLoader classLoader = null;

    public XStreamTranscoder()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public XStreamTranscoder(final ClassLoader cl)
    {
        xstream = new XStream();
        xstream.setClassLoader(cl);
        classLoader = cl;
    }

    public byte[] encode(final Object obj) throws TranscoderException
    {
        byte[] raw = null;
        try
        {
            raw = xstream.toXML(obj).getBytes("UTF-8");
        }
        catch (Exception error)
        {
            throw (new TranscoderException(error));
        }
        return raw;
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(final byte[] raw, final Class<T> klass) throws TranscoderException
    {
        T obj = null;
        try
        {
            obj = (T) xstream.fromXML(new String(raw, "UTF-8"));
        }
        catch (Exception error)
        {
            throw (new TranscoderException(error));
        }
        return obj;
    }
}
