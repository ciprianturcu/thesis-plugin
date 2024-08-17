
# CodeCom - IntelliJ IDEA plugin

This plugin allows developers to streamline the documentation process. The CodeCom Plugin is an IntelliJ IDEA plugin that integrates with a remote server to deliver contextual comments directly into the editor. This plugin enhances the development process by providing immediate visibility into the codebase, specifically indicating which methods are accompanied by comments and which are not. Whether for individual developers or teams, this tool promotes thorough documentation and aims to ensure code clarity.




## Features

- **Real-Time Code Comments**: generate contextual comments directly within the IntelliJ IDEA editor, improving code understanding and collaboration.
- **Server Integration**: Establish a connection with a remote server to retrieve, synchronize, and manage comments across the entire codebase.
- **Dedicated Comment Panel**: Access a dedicated panel that lists all methods with associated comments status, allowing for efficient navigation and review. 
- **Method Comment Status**: Distinguish between documented and undocumented methods, facilitating comprehensive code review and maintenance.
- **Live Update of Structural Changes**: The dedicated panel for comment status review updates to reflect changes made to the structure of the project (Directories, Files,  Methods, Comments).


## Installation

Prerequisites

 - Intellij IDEA 2023.1 or later.
 - Download to local storage the jre contained in the releases section.

Steps 
 
 - Navigate to `File` > `Settings` > `Plugin`
 - Select the gear icon and use the `Install Plugin from Disk` option.
 - Navigate to the local copy of the jre.
 - Click `Ok`.
 - Apply the changes.
 - Restart IntelliJ IDEA to activate the plugin.
    
## Usage

- **Connect to the Server**: Post-installation, navigate to `File` > `Settings` > `CodeCom Settings`. Input the server url that contains an endpoint `/getComment` to establish the connection. The default value is `http://0.0.0.0:8080` for local hosting of the [accompanying server](https://github.com/ciprianturcu/thesis-server).

- **Comment Status Overview**: Utilize the CodeCom panel (accessible via right side toolwindow row) to monitor which methods are documented. Clicking on any method within the panel will direct you to its position in the code.

- **Generate comment**: 
  - Directly into the editor window by `selecting the method text partially or in full` > `right-click` > `Comment method`.
  - From the CodeCom toolwindow where `a method node needs to be selected` > `Comment method`.



## License
[MIT License](https://choosealicense.com/licenses/mit/)

Copyright (c) [2024] [Turcu Ciprian-Stelian]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
