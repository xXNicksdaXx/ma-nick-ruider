/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/


import { CommandContext } from '@eclipse-glsp/vscode-integration';
import { configureUVLCommandContributions } from 'uvl-command-contribution';
interface CommandContributionEntry {
    readonly configureCommands: (context: CommandContext) => void;
}

declare const __UVL_COMMAND_CONTRIBUTION_IDS__: readonly string[];

const commandContributionRegistry: Record<string, CommandContributionEntry> = {
    'uvl-default': {
        configureCommands: configureUVLCommandContributions
    }
};

export function configureBuildProfileCommandContributions(context: CommandContext): void {
    for (const contributionId of __UVL_COMMAND_CONTRIBUTION_IDS__) {
        const contribution = commandContributionRegistry[contributionId];
        if (!contribution) {
            throw new Error(`Unknown command contribution id '${contributionId}' configured for build profile.`);
        }
        contribution.configureCommands?.(context);
    }
}




