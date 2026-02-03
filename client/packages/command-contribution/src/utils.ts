/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import * as vscode from 'vscode';

export async function showInput(prefix: string, hint: string): Promise<string | undefined> {
    return vscode.window.showInputBox({
        prompt: prefix,
        placeHolder: hint,
        ignoreFocusOut: true,
        validateInput: async input => {
            if (!input || input.trim().length === 0) {
                return 'Name cannot be empty';
            }
            const invalidChars = /[<>:"/\\|?*\x00-\x1F\x7F]|[\u{FDD0}-\u{FDEF}]|[\u{FFFE}\u{FFFF}]/gu;
            if (invalidChars.test(input)) {
                return 'Name contains invalid characters';
            }
            return undefined;
        }
    });
}

export function getWorkspaceRoot(): vscode.Uri {
    const workspaceFolders = vscode.workspace.workspaceFolders;
    if (!workspaceFolders || workspaceFolders.length === 0) {
        vscode.window.showErrorMessage('No workspace folder is open. Please open a folder first.');
        return vscode.Uri.file('');
    }
    return workspaceFolders[0].uri;
}

export function getUvlTemplate(): string {
    return `features\n\t\nconstraints\n\t\n`;
}
