# GraphProject

Repository URL: https://github.com/PlasmaDiffusion/GraphProject


Contributors:

Scott Cooper

-Prototyped the UI

-Added base functionality to add, remove and name graphs and add individual points

-Merged final branches together


Jude Antony

-Built on UI design

-Added equation plotting capabilities, zooming and navigation using buttons on graph
	-Navigation feature also supports key presses

-Also included loader/splash screen prior to launching main program


Kenny Le

-Coded file Input/Output

-Added New/Save/Save-As/Open/Close menu buttons and shortcuts (ctrl+N,S,O,etc) and functionality for each
	-Saves/Opens graph data of both equations and data points as .csv or indicated extension
	-Includes many standard program Save/Open features (filepath at menu bar, "*" shown when file was edited since last save)
	

Ellivro Guevarra 

-Constructed socket I/O and networking

To run the client and server, just use gradle to build each of them. The only gradle task used for each is 'run'.
