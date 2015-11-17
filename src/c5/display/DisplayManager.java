package c5.display;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

	private int vaoId, vboPosId, vboColId, vboIndexId;
	private int vsId, fsId, pId;

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
 
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Chapter 5 - Colors", NULL, NULL);
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
        
        defineSquare();
        
        initShaders();
    }
	
	private void loop() {
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT);
            
            renderSquare();
            
            glfwSwapBuffers(window);
 
            glfwPollEvents();
            
            monitorFrameRate();
        }
    }
	
	private void cleanUp() {
		GL30.glBindVertexArray(vaoId);
    	GL20.glDisableVertexAttribArray(0);
    	GL20.glDisableVertexAttribArray(1);
    	GL30.glBindVertexArray(0);
    	GL30.glDeleteVertexArrays(vaoId);
    	
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	GL15.glDeleteBuffers(vboPosId);
    	GL15.glDeleteBuffers(vboColId);
    	 
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboIndexId);
		
		GL20.glUseProgram(0); // unbind the shader program
		GL20.glDetachShader(pId, vsId); // detach shaders before deleting them
		GL20.glDetachShader(pId, fsId);
		GL20.glDeleteShader(vsId); // delete each shader
		GL20.glDeleteShader(fsId);
		GL20.glDeleteProgram(pId); // delete the program
	}
 
    private void defineSquare() {
    	// feeding the shader 4 dimensional vectors is faster,
    	// than converting 3 dimensional ones in the shader
    	// the fourth number (W) is used in matrix operations
    	float[] positions = {
	        -0.5f, 0.5f, 0.0f, 1.0f,	// 0 - top left
	        -0.5f, -0.5f, 0.0f, 1.0f,	// 1 - bottom left
	        0.5f, -0.5f, 0.0f, 1.0f,	// 2 - bottom right
	        0.5f, 0.5f, 0.0f, 1.0f };	// 3 - top right
        	
    	FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(positions.length);
    	positionBuffer.put(positions);
    	positionBuffer.flip();
    	
    	// the fourth dimension is the alpha channel here
    	float[] colors = {
    		0.0f, 1.0f, 0.0f, 1.0f,		// 0 - green
    		0.0f, 0.0f, 1.0f, 1.0f,		// 1 - blue
    		1.0f, 1.0f, 1.0f, 1.0f,		// 2 - white
    		1.0f, 0.0f, 0.0f, 1.0f };	// 3 - red
    	
    	FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(colors.length);
    	colorBuffer.put(colors);
    	colorBuffer.flip();
    	
    	byte[] indices = {
	        0, 1, 2,	// Left bottom triangle
	        0, 2, 3 };	// Right top triangle
    	
    	ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length);
    	indicesBuffer.put(indices);
    	indicesBuffer.flip();
    	
    	vaoId = GL30.glGenVertexArrays();
    	GL30.glBindVertexArray(vaoId);
    	 
    	vboPosId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPosId);
    	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
    	GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0); // positions are size 4 now
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	
    	vboColId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColId);
    	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
    	GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0); // colors VBO is on index 1
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	
    	GL30.glBindVertexArray(0);
    	
    	vboIndexId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexId);
    	GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    private void initShaders() {
		vsId = loadShader("src/c5/shaders/vertex.sh", GL20.GL_VERTEX_SHADER);
		fsId = loadShader("src/c5/shaders/fragment.sh", GL20.GL_FRAGMENT_SHADER);
		
		// you will need a shader program to run individual shaders together
		// you also need to attach them to the program
		pId = GL20.glCreateProgram();
		GL20.glAttachShader(pId, vsId);
		GL20.glAttachShader(pId, fsId);
		
		// tell your program the index of the VBO,
		// which stores data relevant to the
		// input variable of a shader
		GL20.glBindAttribLocation(pId, 0, "in_Position");
		GL20.glBindAttribLocation(pId, 1, "in_Color");
		
		// link and validate are the last necessary steps
		GL20.glLinkProgram(pId);
		GL20.glValidateProgram(pId);
	}

	private void renderSquare() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		GL20.glUseProgram(pId); // bind the shader program
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexId);
		 
		GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE, 0);
		 
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		GL20.glUseProgram(0); // unbind it after using
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
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
	
	// a general function for loading shaders
	// written by Mathias Verboven
	public int loadShader(String filename, int type) {
	    StringBuilder shaderSource = new StringBuilder();
	    int shaderID;
	     
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(filename));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            shaderSource.append(line).append("\n");
	        }
	        reader.close();
	    } catch (IOException e) {
	        System.err.println("Could not read file.");
	        e.printStackTrace();
	        System.exit(-1);
	    }
	     
	    shaderID = GL20.glCreateShader(type);
	    GL20.glShaderSource(shaderID, shaderSource);
	    GL20.glCompileShader(shaderID);
	     
	    return shaderID;
	}
 
    public static void main(String[] args) {
        new DisplayManager().run();
    }
    
}
