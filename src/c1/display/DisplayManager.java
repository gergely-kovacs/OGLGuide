package c1.display;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class DisplayManager {
	private final int SCREEN_WIDTH = 300, SCREEN_HEIGHT = 300; // the screen resolution
	
    private long window; // this is the handle for the window created
    
    public void run() {
        try {
            init(); // initialize the context and set some options
            loop(); // the main loop of the program, this is repeated till the program is closed
 
            glfwDestroyWindow(window); // destroys the specified window
        } finally {
            glfwTerminate(); // frees remaining resources and destroys window and cursor
        }
    }
 
    private void init() {
        if ( glfwInit() != GL11.GL_TRUE ) // initialize GLFW and check if it was successful
            throw new IllegalStateException("Unable to initialize GLFW!");
 
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Chapter 1 - A window", NULL, NULL); // create a window
        if ( window == NULL ) // check if window creation was successful
            throw new RuntimeException("Failed to create the GLFW window!");
 
        glfwMakeContextCurrent(window); // make the context of window the current OpenGL context
        GL.createCapabilities(); // determines what OpenGL functionality will be available to you based on your current context
        
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // sets the clear color bit, this color will be used to clear the screen
    }
 
    private void loop() {
        while ( glfwWindowShouldClose(window) == GL_FALSE ) { // it runs while closing the window is not requested
            glClear(GL_COLOR_BUFFER_BIT); // clear the screen
 
            glfwSwapBuffers(window); // swap front and back buffers
 
            glfwPollEvents(); // poll inputs (mouse and keyboard)
        }
    }
 
    public static void main(String[] args) {
        new DisplayManager().run(); // create a new object of the DisplayManager and run it
    }
    
}
