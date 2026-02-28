# UVL GLSP Diagram Editor

This project provides a diagram editor for the **Universal Variability Language (UVL)**, built using the [Eclipse GLSP](https://github.com/eclipse-glsp/glsp) framework. 
It includes:

- 🖥️ A **Java-based GLSP server** for handling diagram-specific logic
- 🖼️ A **VS Code extension** for integrating the diagram editor into Visual Studio Code

UVL is a concise, extensible language for modeling variability in software product lines.
UVL specifies variability models with a tree-like structure to represent the hierarchical structure of variability models.

## Project Structure

The project is organized as follows:

- **[`server/`](./server):** Java-based Eclipse GLSP server for UVL diagram editing and BP-extension
  - **[`uvl.glsp/`](./server/uvl.glsp):** Eclipse GLSP server for diagram editing
- **[`client/`](./client):** TypeScript/JavaScript client applications and extensions
    - **[`vscode/`](./client/vscode):** VS Code extension
        - **[`extension/`](./client/vscode/extension):** Extension host process
        - **[`webview/`](./client/vscode/webview):** Webview diagram editor
    - **[`packages/`](./client/packages):** Shared packages and components
        - **[`common/`](./client/packages/common):** Shared types and utilities
        - **[`command-contribution/`](./client/packages/command-contribution):** VS Code command handlers
        - **[`sprotty/`](./client/packages/sprotty):** Core diagram rendering
    - **[`workspace/`](./client/workspace):** Example workspace

## Prerequisites

Ensure the following are installed on your system:

- [Node.js](https://nodejs.org/en/) `>=20 <21`
- [Yarn](https://classic.yarnpkg.com/en/docs/install#debian-stable) `>=1.7.0 <2`
- [Java](https://dev.java/) `>=21`
- [Maven](https://maven.apache.org/) `>=3.6.0`

This project is compatible with VS Code `>=1.74.0`.

## Building the Project

The project consists of two main components that need to be built separately: the Java server and the TypeScript/JavaScript client applications.

### Quick Build (All Components)

Use the provided build scripts to build both server and client:

**Windows**
```powershell
.\build.ps1
```

**Linux/macOS**
```bash
./build.sh
```

### Build specific components

You can also build individual components:

#### Java Server

```powershell
.\build.ps1 server  # Windows
```

```bash
./build.sh server  # Linux/macOS
```

```bash
cd server
mvn clean verify
```

#### Client Applications

```powershell
.\build.ps1 client  # Windows
```

```bash
./build.sh client  # Linux/macOS
```

```bash
cd client
yarn
```

```bash
cd client && yarn build:theia  # Build Theia application
```

```bash
cd client && yarn build:vscode  # Build VS Code extension
```

### Clean Build Artifacts

To clean build artifacts, run:

```powershell
.\build.ps1 clean  # Windows
```
```bash
./build.sh clean  # Linux/macOS
```
```bash
cd server && mvn clean  # Clean Java server artifacts
```
```bash
cd client && yarn clean  # Clean client artifacts
```

## Running the Project

### IntelliJ IDEA

The project includes IntelliJ IDEA configuration files in the `.idea/` directory with pre-configured run configurations:

- **Launch UVL GLSP Server**: Starts the Java GLSP server on port 5007. Use this configuration for debugging server-side code.

### VS Code

Use the launch configurations defined in `.vscode/launch.json`. Open the project in Visual Studio Code and navigate to the **Run and Debug** view (`Ctrl + Shift + D`). Choose one of the following launch configurations:

- **Launch UVL GLSP Server**: Starts the standalone Java GLSP server. Use this configuration for debugging server-side code. _Note:_ There is also a run configuration available for JetBrains-IDEs.

- **VS Code Extension Configurations**:
  - **Launch UVL VS Code Extension (Embedded GLSP Server)**: 
    Starts a second VS Code instance with the extension installed. Opens an example workspace containing a `.uvl` file. The GLSP server runs as an embedded process. _Note:_ Server-side debugging is not available in this mode.
  - **Launch UVL VS Code Extension (Excluding GLSP Server)**:
    Similar to the above, but expects the GLSP server to be started externally using the **Launch UVL GLSP Server** configuration. This allows debugging both the client and server code.
  - **Launch UVL VS Code extension (Using external GLSP Server)**: 
    Launches both the UVL GLSP Server and the VS Code extension in external server mode. This enables simultaneous debugging of both the client and server code.

## Packaging the VS Code Extension

To package the VS Code extension as a `.vsix` file, run:

```bash
cd client
yarn package
```

The resulting `.vsix` file will be located in the `vscode/extension/dist` directory and can be installed in VS Code.

## Resources

- [Project GitLab Repository](https://git-st.inf.tu-dresden.de/stgroup/student-projects/2026/ma-nick-ruider)
- [Eclipse GLSP](https://www.eclipse.org/glsp/)
- [Universal Variability Language (UVL)](https://universal-variability-language.github.io/)
