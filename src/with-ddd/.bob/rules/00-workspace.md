# Workspace rules (mandatory)

- Work only inside this workspace folder. Do not read, list or write files outside it.
- Read and list files only with your file tools. Shell commands that read or list files (`cat`, `ls`, `grep`, `head`, `find`…) are denied; the only commands you may run are `mvn`, `java` and `date`.
- Do not run git commands. Version control is handled outside this session.
- Do not use the network beyond what Maven needs to resolve the build: no curl, no wget, no web searches.
- Do not read or print environment variables.
