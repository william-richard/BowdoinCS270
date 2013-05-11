
/*
   
  This class extends an objectdraw library graphics window class 
  (which creates a graphics window) and instantiates a TilePuzzle
  (which extends the objectdraw library ActiveObject class).


  Stephen M. Majercik
  9/13/2007
   

 */


import objectdraw.*;


public class TilePuzzleController extends FrameWindowController {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// create the puzzle and send in the DrawingCanvas
    public void begin() {
	// use this to set the size of the graphics window
	this.resize(400,410);
        new TilePuzzle(canvas);
    } 
    

}
