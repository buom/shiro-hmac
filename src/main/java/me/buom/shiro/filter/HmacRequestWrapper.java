package me.buom.shiro.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ebab on 3/21/14.
 */
class HmacRequestWrapper extends HttpServletRequestWrapper implements HttpServletRequest {

    private transient static final Logger log = LoggerFactory.getLogger(HmacRequestWrapper.class);

    private byte[] entity;

    private int numCalls = 0;

    public HmacRequestWrapper(HttpServletRequest request) {
        super(request);

        try {
            this.entity = toByteArray(request.getInputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        if (numCalls > 1) {
            entity = new byte[0];
        }
        numCalls++;
        //log.trace("--> getInputStream -> " + new String(entity));
        //log.debug("numCalls -> {}, entity -> {}", numCalls, entity);
        return new ServletInputStreamImpl(new ByteArrayInputStream(entity));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String enc = getCharacterEncoding();
        if (enc == null) {
            enc = "UTF-8";
        }
        return new BufferedReader(new InputStreamReader(getInputStream(), enc));
    }

    protected byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (inputStream != null) {
            byte[] buff = new byte[2048];
            int count = -1;
            while ((count = inputStream.read(buff)) > 0) {
                byteArrayOutputStream.write(buff, 0, count);
            }
            inputStream.close();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /** ===================================================================== **/

    static class ServletInputStreamImpl extends ServletInputStream {

        private InputStream inputStream;

        public ServletInputStreamImpl(InputStream iis) {
            this.inputStream = iis;
        }

        @Override
        public int read() throws IOException {
            return this.inputStream.read();
        }
    }
}
