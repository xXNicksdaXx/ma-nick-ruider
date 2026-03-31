/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import { CommandContext } from '@eclipse-glsp/vscode-integration';
import { promises as fs } from 'fs';
import * as vscode from 'vscode';

import { getUvlTemplate, getWorkspaceRoot, showInput } from './utils';

export function configureUVLCommandContributions(context: CommandContext) {
    // keep track of diagram specific element selection.
    const { extensionContext, diagramPrefix } = context;

    extensionContext.subscriptions.push(
        vscode.commands.registerCommand(`${diagramPrefix}.newDiagram`,
            async () => await createNewDiagramFromPrompt()
        )
    );
}

async function createNewDiagramFromPrompt(): Promise<void> {
    const modelName = await showInput(
        'Enter name of new UVL file',
        'UVL Name');

    const workspaceUri = getWorkspaceRoot();
    if (modelName) {
        const uvlFileUri = vscode.Uri.joinPath(workspaceUri, `${modelName}.uvl`);
        const notationFileUri = vscode.Uri.joinPath(workspaceUri, `${modelName}.notation.json`);

        try {
            await fs.access(uvlFileUri.fsPath);
            vscode.window.showErrorMessage(`File ${uvlFileUri.fsPath} already exists.`);
            return;
        } catch {
            // File does not exist, continue
        }

        try {
            await fs.access(notationFileUri.fsPath);
            vscode.window.showErrorMessage(`File ${notationFileUri.fsPath} already exists.`);
            return;
        } catch {
            // File does not exist, continue
        }

        try {
            await fs.writeFile(uvlFileUri.fsPath, getUvlTemplate(),'utf-8');
            await vscode.workspace.openTextDocument(uvlFileUri);
        } catch (error) {
            vscode.window.showErrorMessage(`Failed to create files: ${error}`);
            return;
        }

        vscode.window.showInformationMessage(`Created new UVL file: ${uvlFileUri.fsPath.toString()}`);
    }
}
