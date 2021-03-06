package external.apiai.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import external.apiai.util.IOUtils;

public class HttpClient {

	private static final Logger Log = LogManager.getLogger(HttpClient.class);
    private static final int CHUNK_LENGTH = 2048;
    private static final int BUFFER_LENGTH = 4096;

    /**
     *  Cannot be <code>null</code>
     */
    private final HttpURLConnection connection;
    private OutputStream os;

    private final String delimiter = "--";
    private final String boundary = "SwA" + Long.toString(System.currentTimeMillis()) + "SwA";

    private boolean writeSoundLog;

    /**
     * @param connection Cannot be <code>null</code>
     */
    public HttpClient(final HttpURLConnection connection) {
    	if (connection == null) {
    		throw new IllegalArgumentException("Connection cannot be null");
    	}
        this.connection = connection;
    }

    public void connectForMultipart() throws IOException {
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setChunkedStreamingMode(CHUNK_LENGTH);
        connection.connect();
        os = connection.getOutputStream();
    }

    /**
     * @param paramName  Cannot be <code>null</code>
     * @param value  Cannot be <code>null</code>
     * @throws IOException
     */
    public void addFormPart(final String paramName, final String value) throws IOException {
        os.write((delimiter + boundary + "\r\n").getBytes());
        os.write("Content-Type: application/json\r\n".getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
        os.write(("\r\n" + value + "\r\n").getBytes());
    }

    /**
     * @param paramName Cannot be <code>null</code>
     * @param fileName Cannot be <code>null</code>
     * @param data Cannot be <code>null</code>
     * @throws IOException
     */
    public void addFilePart(final String paramName, final String fileName, final InputStream data) throws IOException {
        os.write((delimiter + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
        os.write(("Content-Type: audio/wav\r\n").getBytes());
        //os.write( ("Content-Transfer-Encoding: binary\r\n"  ).getBytes());
        os.write("\r\n".getBytes());

        Log.debug("Sound write start");

        FileOutputStream outputStream = null;

        if (writeSoundLog) {
            final File cacheDir = new File(System.getProperty("java.io.tmpdir"));
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            Log.debug(cacheDir.getAbsolutePath());

            final File soundFile = new File(cacheDir, "log.wav");
            outputStream = new FileOutputStream(soundFile, false);
        }

        final byte[] buffer = new byte[BUFFER_LENGTH];

        int bytesActuallyRead;

        bytesActuallyRead = data.read(buffer, 0, buffer.length);

        while (bytesActuallyRead >= 0) {
            if (bytesActuallyRead > 0) {
                os.write(buffer, 0, bytesActuallyRead);

                if (writeSoundLog) {
                    outputStream.write(buffer, 0, bytesActuallyRead);
                }
            }
            bytesActuallyRead = data.read(buffer, 0, buffer.length);
        }

        if (writeSoundLog) {
            outputStream.close();
        }

        Log.debug("Sound write finished");

        os.write("\r\n".getBytes());
    }

    public void finishMultipart() throws IOException {
        os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
        os.close();
    }

    /**
     * @return Response string. Never <code>null</code>
     * @throws IOException
     */
    public String getResponse() throws IOException {
        final InputStream inputStream = new BufferedInputStream(connection.getInputStream());
        final String response = IOUtils.readAll(inputStream);
        inputStream.close();
        return response;
    }

    public String getErrorString() {
        try {
            final InputStream inputStream = new BufferedInputStream(connection.getErrorStream());
            final String response;
            response = IOUtils.readAll(inputStream);
            inputStream.close();
            return response;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setWriteSoundLog(final boolean writeSoundLog) {
        this.writeSoundLog = writeSoundLog;
    }
}