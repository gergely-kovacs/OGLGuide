package c4.display;

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

	private int vaoId, vboVertexId, vboIndexId;

    public void run() {
        try {
            init();
            loop();
            cleanUp();
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
        }
    }
 
	private void init() {
        if ( glfwInit() != GL11.GL_TRUE )
            throw new IllegalStateException("Unable to initialize GLFW!");
        
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
 
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Chapter 4 - Indexing", NULL, NULL);
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
        
        defineSquareWithIndexing();
    }
	
	private void loop() {
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT);
            
            renderSquareWithDrawElements();
            
            glfwSwapBuffers(window);
 
            glfwPollEvents();
            
            monitorFrameRate();
        }
    }
	
	private void cleanUp() {
		GL30.glBindVertexArray(vaoId);
    	GL20.glDisableVertexAttribArray(0);
    	GL30.glBindVertexArray(0);
    	GL30.glDeleteVertexArrays(vaoId);
    	
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	GL15.glDeleteBuffers(vboVertexId);
    	
    	if (vboIndexId != 0) {
    		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    		GL15.glDeleteBuffers(vboIndexId);
    	}
	}
 
	// this defines 6 vertices in order to make a square
	// it requires the usage of glDrawArrays
	// the adequate rendering function is renderSquareWithDrawArrays
	// top left and bottom right are used twice
    @SuppressWarnings("unused")
	private void defineSquareWithoutIndexing() {
    	float[] vertices = {
	        -0.5f, 0.5f, 0f,	// top left
	        -0.5f, -0.5f, 0f,	// bottom left
	        0.5f, -0.5f, 0f,	// bottom right
	        -0.5f, 0.5f, 0f,	// top left
	        0.5f, -0.5f, 0f,	// bottom right
	        0.5f, 0.5f, 0f };	// top right
    	
    	FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
    	verticesBuffer.put(vertices);
    	verticesBuffer.flip();
    	
    	vaoId = GL30.glGenVertexArrays();
    	GL30.glBindVertexArray(vaoId);
    	 
    	vboVertexId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexId);
    	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
    	GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	 
    	GL30.glBindVertexArray(0);
	}
    
    // this one uses an indexing vbo, it goes with renderSquareWithDrawElements
    private void defineSquareWithIndexing() {
    	float[] vertices = {
	        -0.5f, 0.5f, 0f,	// 0 - top left
	        -0.5f, -0.5f, 0f,	// 1 - bottom left
	        0.5f, -0.5f, 0f,	// 2 - bottom right
	        0.5f, 0.5f, 0f };	// 3 - top right
        	
    	FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
    	verticesBuffer.put(vertices);
    	verticesBuffer.flip();
    	
    	byte[] indices = {
	        0, 1, 2,	// Left bottom triangle
	        0, 2, 3 };	// Right top triangle
    	
    	ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length);
    	indicesBuffer.put(indices);
    	indicesBuffer.flip();
    	
    	vaoId = GL30.glGenVertexArrays();
    	GL30.glBindVertexArray(vaoId);
    	 
    	vboVertexId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexId);
    	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
    	GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	
    	GL30.glBindVertexArray(0);
    	
    	// note that the GL_ELEMENT_ARRAY_BUFFER VBO doesn't need glVertexAttribPointer
    	// thus it is not stored in the VAO
    	// which means, you will have to bind it separately
    	vboIndexId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexId);
    	GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

	@SuppressWarnings("unused")
	private void renderSquareWithDrawArrays() {
		GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);
         
        // the "count" parameter was changed from 3 to 6, since we are now drawing two triangles
        // (which obviously consist of 6 vertices)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
         
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
	}
	
	private void renderSquareWithDrawElements() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexId); // bind index VBO
		 
		// the indexing VBO is fed from a byte array, thus GL_UNSIGNED_BYTE is used here
		// there is no unsigned in java, but OpenGL expects this constant anyway
		GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE, 0);
		 
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
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
