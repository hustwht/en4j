/*
 *  Copyright (C) 2010 Ruben Laguna <ruben.laguna@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.rubenlaguna.en4j.noterepository;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author Ruben Laguna <ruben.laguna@gmail.com>
 */
public class DbPstmts {

    private static final Logger LOG = Logger.getLogger(DbPstmts.class.getName());
    private static DbPstmts theInstance = null;
    private static boolean closed = false;
    final PreparedStatementWrapper<Integer, String> sourceurlPstmt;
    final PreparedStatementWrapper<Integer, Reader> contentFromNotes;
    final PreparedStatementWrapper<String, InputStream> dataFromResourcesPstmt;
    final PreparedStatementWrapper<String, Integer> dataLengthFromResourcesPstmt;
    final PreparedStatementWrapper<Integer, String> guidFromNotesPstmt;
    final PreparedStatementWrapper<Integer, Date> createdIdFromNotesPstmt;
    final PreparedStatementWrapper<Integer, Date> updatedIdFromNotesPstmt;
    final PreparedStatementWrapper<String, byte[]> recogFromResourcesPstmt;
    final PreparedStatementWrapper<String, Integer> usnFromResourcesGuidPstmt;
    final PreparedStatementWrapper<String, String> ownerguidFromResourcesPstmt;
    final PreparedStatementWrapper<String, String> mimeFromResources;
    final PreparedStatementWrapper<String, Collection<String>> hashResourcesOwnerPstmt;
    final PreparedStatementWrapper<Integer, Integer> usnFromNotesPstmt;
    final PreparedStatementWrapper<String, String> filenameFromResourcesPstmt;
    final PreparedStatementWrapper<String, String> hashFromResourcesPstmt;
    final PreparedStatementWrapper<Integer, String> titleFromNotes;
    final PreparedStatementWrapper<Integer, Boolean> isNoteActiveFromId;

    private DbPstmts() throws SQLException {
        contentFromNotes = new PreparedStatementWrapper<Integer, Reader>(getConnection().prepareStatement("SELECT CONTENT FROM NOTES WHERE ID=?")) {

            @Override
            protected Reader getResultFromResulSet(ResultSet rs) throws SQLException {
                final Reader characterStream = rs.getCharacterStream("CONTENT");
                return characterStream;
            }
        };

        sourceurlPstmt = new PreparedStatementWrapper<Integer, String>(getConnection().prepareStatement("SELECT SOURCEURL FROM NOTES WHERE ID =?")) {

            @Override
            protected String getResultFromResulSet(ResultSet rs) throws SQLException {
                final String toReturn = rs.getString("SOURCEURL");
                return toReturn;
            }
        };
        titleFromNotes = new PreparedStatementWrapper<Integer, String>(getConnection().prepareStatement("SELECT TITLE FROM NOTES WHERE ID =?")) {

            @Override
            protected String getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getString("TITLE");
            }
        };
        createdIdFromNotesPstmt = new PreparedStatementWrapper<Integer, Date>(getConnection().prepareStatement("SELECT CREATED FROM NOTES WHERE ID =?")) {

            @Override
            protected Date getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getTimestamp("CREATED");
            }
        };
        updatedIdFromNotesPstmt = new PreparedStatementWrapper<Integer, Date>(getConnection().prepareStatement("SELECT UPDATED FROM NOTES WHERE ID =?")) {

            @Override
            protected Date getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getTimestamp("UPDATED");
            }
        };

        usnFromNotesPstmt = new PreparedStatementWrapper<Integer, Integer>(getConnection().prepareStatement("SELECT USN FROM NOTES WHERE ID =?")) {

            @Override
            protected Integer getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getInt("USN");
            }
        };
        hashResourcesOwnerPstmt = new PreparedStatementWrapper<String, Collection<String>>(getConnection().prepareStatement("SELECT HASH FROM RESOURCES WHERE OWNERGUID =?")) {

            @Override
            protected Collection<String> getResultFromResulSet(ResultSet rs) throws SQLException {
                Collection<String> toReturn = new ArrayList<String>();
                do {
                    toReturn.add(rs.getString("HASH"));
                } while (rs.next());
                return toReturn;
            }
        };
        hashFromResourcesPstmt = new PreparedStatementWrapper<String, String>(getConnection().prepareStatement("SELECT HASH FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected String getResultFromResulSet(ResultSet rs) throws SQLException {
                    return rs.getString("HASH");
            }
        };
        guidFromNotesPstmt = new PreparedStatementWrapper<Integer, String>(getConnection().prepareStatement("SELECT GUID FROM NOTES WHERE ID =?")) {

            @Override
            protected String getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getString("GUID");
            }
        };
        dataFromResourcesPstmt = new PreparedStatementWrapper<String, InputStream>(getConnection().prepareStatement("SELECT DATA FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected InputStream getResultFromResulSet(ResultSet rs) throws SQLException {
                final Blob blob = rs.getBlob("DATA");
                final InputStream is = blob.getBinaryStream();
                return is;
            }
        };
        dataLengthFromResourcesPstmt = new PreparedStatementWrapper<String, Integer>(getConnection().prepareStatement("SELECT LENGTH(DATA) FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected Integer getResultFromResulSet(ResultSet rs) throws SQLException {
                final int toReturn = rs.getInt(1);
//                final InputStream is = blob.getBinaryStream();
                return toReturn;
            }
        };
        mimeFromResources = new PreparedStatementWrapper<String, String>(getConnection().prepareStatement("SELECT MIME FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected String getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getString("MIME");
            }
        };

        ownerguidFromResourcesPstmt = new PreparedStatementWrapper<String, String>(getConnection().prepareStatement("SELECT OWNERGUID FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected String getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getString("OWNERGUID");
            }
        };
        recogFromResourcesPstmt = new PreparedStatementWrapper<String, byte[]>(getConnection().prepareStatement("SELECT RECOGNITION FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected byte[] getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getBytes("RECOGNITION");
            }
        };
        usnFromResourcesGuidPstmt = new PreparedStatementWrapper<String, Integer>(getConnection().prepareStatement("SELECT USN FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected Integer getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getInt("USN");
            }
        };
        filenameFromResourcesPstmt = new PreparedStatementWrapper<String, String>(getConnection().prepareStatement("SELECT FILENAME FROM RESOURCES WHERE GUID=?")) {

            @Override
            protected String getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getString("FILENAME");
            }
        };
        isNoteActiveFromId = new PreparedStatementWrapper<Integer, Boolean>(getConnection().prepareStatement("SELECT ISACTIVE FROM NOTES WHERE ID=?")) {

            @Override
            protected Boolean getResultFromResulSet(ResultSet rs) throws SQLException {
                return rs.getBoolean("ISACTIVE");
            }
        };
    }

    public Reader getContentAsReader(int id) {
        return contentFromNotes.get(id);
    }

    public String getSourceurl(int id) {
        return sourceurlPstmt.get(id);
    }

    public String getTitle(int id) {
        return titleFromNotes.get(id);
    }
    public Date getCreated(int id) {
        return createdIdFromNotesPstmt.get(id);
    }
    public Date getUpdated(int id) {
        return updatedIdFromNotesPstmt.get(id);
    }

    public int getUpdateSequenceNumber(int id) {
        return usnFromNotesPstmt.get(id);
    }

    public Collection<String> getResources(String guid) {
        final Collection<String> resources = hashResourcesOwnerPstmt.get(guid);
        if (resources == null) {
            return Collections.EMPTY_LIST;
        }
        final Collection<String> toReturn = new HashSet<String>(resources.size());
        for (String hash : resources) {
            toReturn.add(padded(hash));
        }
        return toReturn;
    }

    private String padded(String hash) {
        if (hash.length() == 32) {
            return hash;
        }
        if (hash.length() < 32) {
            //if we (incorrectly stored the hash without the left paddind we will add it now
            StringBuilder sb = new StringBuilder();
            for (int i = hash.length(); i < 32; i++) {
                sb.append("0");
            }
            sb.append(hash);
            final String paddedHash = sb.toString();
            LOG.log(Level.WARNING, "padding hash to {0} length:{1}", new Object[]{paddedHash, paddedHash.length()});
            return paddedHash;
        }
        return hash;
    }

    public String getGuid(int id) {
        return guidFromNotesPstmt.get(id);
    }

    public String getDataHash(String resGuid) {
        final String hash = hashFromResourcesPstmt.get(resGuid);
        return padded(hash);
    }

    public InputStream getDataAsInputStream(String guid) {
        return dataFromResourcesPstmt.get(guid);
    }

    public byte[] getData(String resGuid) {
        try {
            BufferedInputStream is = new BufferedInputStream(getDataAsInputStream(resGuid));
            ReadableByteChannel isc = Channels.newChannel(is);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            WritableByteChannel osc = Channels.newChannel(os);
            ByteBuffer bb = ByteBuffer.allocate(32000);
            while (isc.read(bb) != -1) {
                bb.flip();
                osc.write(bb);
                bb.clear();
            }
            return os.toByteArray();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public int getDataLength(String guid) {
        return dataLengthFromResourcesPstmt.get(guid);
    }

    public String getFilename(String resGuid) {
        return filenameFromResourcesPstmt.get(resGuid);
    }

    public String getMime(String guid) {
        return mimeFromResources.get(guid);
    }

    public String getNoteguid(String resGuid) {
        return ownerguidFromResourcesPstmt.get(resGuid);
    }

    public byte[] getRecognition(String resGuid) {
        return recogFromResourcesPstmt.get(resGuid);
    }

    int getUpdateSequenceNumberForResource(String resGuid) {
        return usnFromResourcesGuidPstmt.get(resGuid);
    }

    boolean isActive(int id) {
        return isNoteActiveFromId.get(id);
    }

    public synchronized void close() {
        if (closed) {
            LOG.info("DbPstmts was already closed before");
            return;
        }
        closed = true;
        theInstance = null;

        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            final Class<?> fieldType = field.getType();
            final String fieldName = field.getName();
            LOG.log(Level.INFO, "field {0} has type {1}", new Object[]{fieldName, fieldType});
            if (fieldType.equals(PreparedStatementWrapper.class)) {
                try {
                    Object value = field.get(this);

                    if (value != null && value instanceof PreparedStatementWrapper) {
                        PreparedStatementWrapper toClose = (PreparedStatementWrapper) value;
                        LOG.log(Level.INFO, "closing field {0}", fieldName);
                        toClose.close();
//                        LOG.log(Level.INFO, "set field {0} to null", fieldName);
//                        field.set(this, null);
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.SEVERE, "exception field '" + fieldName + "'", ex);
                } catch (IllegalAccessException ex) {
                    LOG.log(Level.SEVERE, "exception field '" + fieldName + "'", ex);
                }
            }
        }
    }

    private Connection getConnection() {
        return Installer.c;
    }

    public synchronized static DbPstmts getInstance() {
        if (closed) {
            throw new IllegalStateException("DbPstmts already closed.");
        }
        try {
            if (theInstance == null && !closed) {
                theInstance = new DbPstmts();
            }
            return theInstance;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private Logger getLogger() {
        return LOG;

    }

}
