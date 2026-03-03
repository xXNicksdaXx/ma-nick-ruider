/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/

import 'reflect-metadata';

import {
    configureDefaultCommands,
    GlspSocketServerLauncher,
    GlspVscodeConnector,
    SocketGlspVscodeServer
} from '@eclipse-glsp/vscode-integration/node';
import { configureUVLCommandContributions } from 'uvl-command-contribution';
import * as path from 'path';
import * as process from 'process';
import * as vscode from 'vscode';
import UVLEditorProvider from "./uvl-editor-provider";

export const LOG_DIR = path.join(__dirname, '..', 'logs');

const DEFAULT_SERVER_PORT = '0';

export async function activate(context: vscode.ExtensionContext): Promise<void> {
    // Start server process using quickstart component
    let serverProcess: GlspSocketServerLauncher | undefined;
    const port = JSON.parse(process.env.GLSP_SERVER_PORT || DEFAULT_SERVER_PORT);

    if (process.env.GLSP_SERVER_DEBUG !== 'true') {
        const modulePath = vscode.Uri.joinPath(context.extensionUri, 'dist', 'uvl-0.2.0-glsp.jar').fsPath;
        serverProcess = new GlspSocketServerLauncher({
            executable: modulePath,
            socketConnectionOptions: { port: port },
            additionalArgs: [
                '--consoleLog', 'true',
                '--fileLog', 'false',
                '--logDir', LOG_DIR
            ],
            logging: true
        });
        context.subscriptions.push(serverProcess);
        await serverProcess.start();
    }

    // Wrap server with quickstart component
    const minimalServer = new SocketGlspVscodeServer({
        clientId: 'glsp.uvl',
        clientName: 'universal-variability-language',
        connectionOptions: { port: serverProcess?.getPort() || port }
    });

    // Initialize GLSP-VSCode connector with server wrapper
    const glspVscodeConnector = new GlspVscodeConnector({
        server: minimalServer,
        logging: true
    });

    const customEditorProvider = vscode.window.registerCustomEditorProvider(
        'uvl.glspDiagram',
        new UVLEditorProvider(context, glspVscodeConnector),
        {
            webviewOptions: { retainContextWhenHidden: true },
            supportsMultipleEditorsPerDocument: false
        }
    );

    context.subscriptions.push(minimalServer, glspVscodeConnector, customEditorProvider);
    await minimalServer.start();

    configureDefaultCommands({ extensionContext: context, connector: glspVscodeConnector, diagramPrefix: 'uvl' });
    configureUVLCommandContributions({ extensionContext: context, connector: glspVscodeConnector, diagramPrefix: 'uvl' });
}
