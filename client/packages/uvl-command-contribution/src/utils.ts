/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import * as vscode from 'vscode';

function containsInvalidFileNameCharacter(input: string): boolean {
    for (const char of input) {
        const codePoint = char.codePointAt(0);
        if (codePoint === undefined) {
            continue;
        }
        if (codePoint <= 0x1f || codePoint === 0x7f) {
            return true;
        }
        if (codePoint >= 0xfdd0 && codePoint <= 0xfdef) {
            return true;
        }
        if (codePoint === 0xfffe || codePoint === 0xffff) {
            return true;
        }
    }
    return /[<>:"/\\|?*]/.test(input);
}

export async function showInput(prefix: string, hint: string): Promise<string | undefined> {
    return vscode.window.showInputBox({
        prompt: prefix,
        placeHolder: hint,
        ignoreFocusOut: true,
        validateInput: async input => {
            if (!input || input.trim().length === 0) {
                return 'Name cannot be empty';
            }
            if (containsInvalidFileNameCharacter(input)) {
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
