package c3.display;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class DisplayManager {
	private final int SCREEN_WIDTH = 300, SCREEN_HEIGHT = 300;
	
    private long window;
    
    private int frameCount = 0;	
    private double lastTime = glfwGetTime();

	private int vaoId, vboId; // integers used to handle data structures (VAOs and VBOs)
    
    public void run() {
        try {
            init();
            loop();
            cleanUp(); // unbind and delete the buffers after using them
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
        }
    }
 
	private void init() {
        if ( glfwInit() != GL11.GL_TRUE )
            throw new IllegalStateException("Unable to initialize GLFW!");
        
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
 
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Chapter 3 - A triangle", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window!");
        
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window,
            (GLFWvidmode.width(vidmode) - SCREEN_WIDTH) / 2,
            (GLFWvidmode.height(vidmode) - SCREEN_HEIGHT) / 2 );
 
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        
        glfwSwapInterval(0);
        
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        defineVertices(); // feed vertex data to OpenGL
    }
	
	private void loop() {
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT);
            
            renderTriangle(); // draws the triangle onto the screen
            
            glfwSwapBuffers(window);
 
            glfwPollEvents();
            
            monitorFrameRate();
        }
    }
	
	private void cleanUp() {
    	GL30.glBindVertexArray(vaoId); // bind the VAO
    	GL20.glDisableVertexAttribArray(0); // disable VBO with index 0
    	GL30.glBindVertexArray(0); // unbind VAOs
    	GL30.glDeleteVertexArrays(vaoId); // delete this VAO
    	
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // unbind VBOs from GL_ARRAY_BUFFER
    	GL15.glDeleteBuffers(vboId); // delete this VBO
	}
 
    private void defineVertices() {
    	float[] vertices = {
	        -0.5f, -0.5f, 0f, // lower left vertex
	        0.5f, -0.5f, 0f, // lower right vertex
	        0f, 0.5f, 0f }; // top vertex
        	
    	FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length); // allocate memory according to the size of vertices array
    	verticesBuffer.put(vertices); // uploads the vertices to the buffer
    	verticesBuffer.flip(); // flip the buffer, resetting the index to 0, making it ready to be read from
    	
    	vaoId = GL30.glGenVertexArrays(); // generate a VAO and return a handle
    	GL30.glBindVertexArray(vaoId); // bind the VAO (only one VAO can be active at a time)
    	 
    	vboId = GL15.glGenBuffers(); // create a VBO and return a handle
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId); // bind the vbo before specifying its data source
    	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW); // specify the data source and type, it's not gonna change often, so STATIC_DRAW is fine
    	GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0); // set location and structure of the bound attribute
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);	// unbind this VBO
    	 
    	GL30.glBindVertexArray(0);	// unbind the VAO
	}

	private void renderTriangle() {
		GL30.glBindVertexArray(vaoId); // bind VAO
        GL20.glEnableVertexAttribArray(0); // enable VBO index 0
         
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3); // draw the contents of the VAO, 3 is the number of vertices
         
        GL20.glDisableVertexAttribArray(0);	// disable VBO index 0
        GL30.glBindVertexArray(0);	// deactivate the VAO
	}

	private void monitorFrameRate() {
        frameCount++;
        if (glfwGetTime() - lastTime >= 1.0d) {
        	System.out.println(frameCount); // TODO: render using bitmap font
        	lastTime = glfwGetTime();
        	frameCount = 0;
        }
    }
	
	@SuppressWarnings("unused")
	private void monitorProcessingTime() {
        frameCount++;
        if (glfwGetTime() - lastTime >= 1.0d) {
        	System.out.println(1000.0d / (double) frameCount); // TODO: render using bitmap font
        	lastTime = glfwGetTime();
        	frameCount = 0;
        }
    }
 
    public static void main(String[] args) {
        new DisplayManager().run();
    }
    
}
