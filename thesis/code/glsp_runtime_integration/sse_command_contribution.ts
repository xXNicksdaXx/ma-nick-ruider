export function configureSSECommandContributions(context: CommandContext): void {
    const { extensionContext, diagramPrefix, connector } = context;

    extensionContext.subscriptions.push(
        vscode.commands.registerCommand(`${diagramPrefix}.sse.startListening`, () => {
            connector.dispatchAction(SSEStartListeningAction.create());
        }),
        vscode.commands.registerCommand(`${diagramPrefix}.sse.highFrequencyPolling`, () => {
            connector.dispatchAction(SSEHighFrequencyPollingAction.create());
        }),
        vscode.commands.registerCommand(`${diagramPrefix}.sse.stopListening`, () => {
            connector.dispatchAction(SSEStopListeningAction.create());
        })
    );
}