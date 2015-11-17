package c2.display;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class DisplayManager {
	private final int SCREEN_WIDTH = 300, SCREEN_HEIGHT = 300;
	
    private long window;
    
    private int frameCount = 0; // the number of frames processed in a given second
    private double lastTime = glfwGetTime(); // this is used to measure time
    
    public void run() {
        try {
            init();
            loop();
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
        }
    }
 
    private void init() {
        if ( glfwInit() != GL11.GL_TRUE )
            throw new IllegalStateException("Unable to initialize GLFW!");
        
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // make the next window created non-resizable
 
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Chapter 2 - Settings", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window!");
 
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor()); // get the primary monitor's refresh rate, resolution, and color depth
        glfwSetWindowPos(window,
            (GLFWvidmode.width(vidmode) - SCREEN_WIDTH) / 2,
            (GLFWvidmode.height(vidmode) - SCREEN_HEIGHT) / 2 ); // center the window on the primary monitor
        
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        
        glfwSwapInterval(1); // wait 1 monitor refresh before swapping front and back buffers (enable Vsync)
        
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }
 
    private void loop() {
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT);
            
            glfwSwapBuffers(window);
 
            glfwPollEvents();
            
            monitorFrameRate(); // either monitor frame rate (FPS) or monitor the processing time (ms) of this cycle
        }
    }
    
	private void monitorFrameRate() {
        frameCount++; // increase the number of frames
        if (glfwGetTime() - lastTime >= 1.0d) { // if 1 has second passed since last print
        	System.out.println(frameCount); // print the number of frames processed this second to the console // TODO: render it using bitmap font
        	lastTime = glfwGetTime(); // reset the timer
        	frameCount = 0; // reset the number of frames 
        }
    }
	
	@SuppressWarnings("unused")
	private void monitorProcessingTime() {
        frameCount++; // increase the number of frames
        if (glfwGetTime() - lastTime >= 1.0d) { // if 1 has second passed since last print
        	System.out.println(1000.0d / (double) frameCount); // print the average processing time in milliseconds // TODO: render it using bitmap font
        	lastTime = glfwGetTime(); // reset timer
        	frameCount = 0; // reset number of frames
        }
    }
 
    public static void main(String[] args) {
        new DisplayManager().run();
    }
    
}
