/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ServerAPIImpl;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;

/**
 * @author Matthieu Chaffotte
 */
public class ServerAPITcpServer implements ServerAPI {

    private static final long serialVersionUID = 1L;

    private final ServerSocket serverSocket;
    private final ServerAPIImpl apiImpl = new ServerAPIImpl();

    public ServerAPITcpServer(final int port) throws IOException, ClassNotFoundException, ServerWrappedException {
        System.err.println(this.getClass().getSimpleName() + " - starting...");
        serverSocket = new ServerSocket(port);
        System.err.println(this.getClass().getSimpleName() + " - serverSocket build: " + serverSocket);
        while (true) {
            final Socket clientSocket = serverSocket.accept();
            System.err.println(this.getClass().getSimpleName() + " - accepting data on serverSocket, clientSocket: " + clientSocket);
            System.err.println(this.getClass().getSimpleName() + " - starting a new loop...");
            final ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream oos = null;
            try {
                final MethodCall methodCall = (MethodCall) ois.readObject();
                System.err.println(this.getClass().getSimpleName() + " - received methodCall: " + methodCall);
                final Object callResult = invokeMethod(methodCall);
                System.err.println(this.getClass().getSimpleName() + " - callResult: " + callResult);
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(callResult);
                System.err.println(this.getClass().getSimpleName() + " - flushing callResult: " + callResult);
                oos.flush();
            } catch (Throwable t) {
              t.printStackTrace();  
            } finally {
                if (ois != null) {
                    //ois.close();
                }
                if (oos != null) {
                    //oos.close();
                }
            }
        }
    }

    private Object invokeMethod(final MethodCall methodCall) throws ServerWrappedException {
        final Map<String, Serializable> options = methodCall.getOptions();
        final String apiInterfaceName = methodCall.getApiInterfaceName();
        final String methodName = methodCall.getMethodName();
        final List<String> classNameParameters = methodCall.getClassNameParameters();
        final Object[] parametersValues = methodCall.getParametersValues();
        System.err.println(this.getClass().getSimpleName() + " - invoking: with parameters: " 
        + ", options: " + options
        + ", apiInterfaceName: " + apiInterfaceName
        + ", methodName: " + methodName
        + ", classNameParameters: " + classNameParameters
        + ", parametersValues: " + parametersValues
        + "...");
        try {
            return this.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        } catch (ServerWrappedException e) {
            System.err.println(this.getClass().getSimpleName() + " - got an exception during the invokeMethod: " + e.getClass() + ": " + e.getMessage());
            return e;
        }
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        return apiImpl.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
    }

}
