/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.launch;

import de.tu_dresden.inf.st.uvl.glsp.UVLDiagramModule;
import org.apache.commons.cli.ParseException;
import org.eclipse.glsp.server.di.ServerModule;
import org.eclipse.glsp.server.launch.DefaultCLIParser;
import org.eclipse.glsp.server.launch.GLSPServerLauncher;
import org.eclipse.glsp.server.launch.SocketGLSPServerLauncher;
import org.eclipse.glsp.server.utils.LaunchUtil;

/**
 * Entry point for launching the UVL GLSP server as a standalone process.
 * <p>
 * This class is responsible for parsing command-line arguments, configuring the server
 * with the {@link UVLDiagramModule}, and starting a socket-based GLSP server instance.
 * It supports customization of the server's host and port via CLI parameters
 * and provides usage help on error.
 * <p>
 * The launcher is intended to be used as the main class for running
 * the UVL diagram editor backend.
 */
public final class UVLServerLauncher {

    /**
     * Starts the UVL GLSP server with the provided command-line arguments.
     * <p>
     * This method parses the host and port from the arguments, configures the
     * server with the
     * UVL diagram module, and launches the server. If argument parsing fails, it
     * prints the stack trace
     * and displays usage help.
     *
     * @param args Command-line arguments for server configuration, such as
     *             <code>--host</code> and <code>--port</code>
     */
    public static void main(final String[] args) {
        String processName = "UVLGlspServer";
        try {
            // Initialize the CLI parser with the provided arguments
            DefaultCLIParser parser = new DefaultCLIParser(args, processName);

            // Parse the port and host from the CLI arguments
            int port = parser.parsePort();
            String host = parser.parseHostname();

            // Create and configure the server module with the UVL diagram module
            ServerModule UVLServerModule = new ServerModule()
                    .configureDiagramModule(new UVLDiagramModule());

            // Start the GLSP server as a socket server with the specified parameters
            GLSPServerLauncher launcher = new SocketGLSPServerLauncher(UVLServerModule);
            launcher.start(host, port, parser);
        } catch (ParseException ex) {
            // Print the error and display help if argument parsing fails
            ex.printStackTrace();
            LaunchUtil.printHelp(processName, DefaultCLIParser.getDefaultOptions());
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private UVLServerLauncher() {
        // Prevent instantiation
    }
}
