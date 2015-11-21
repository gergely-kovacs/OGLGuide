package c6.display;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class DisplayManager {
	private final int SCREEN_WIDTH = 300, SCREEN_HEIGHT = 300;
	
    private long window;
    
    private int frameCount = 0;	
    private double lastTime = glfwGetTime();

	private int vaoId, vboPosId, vboColId, vboTexId, vboIndexId;
	private int vsId, fsId, pId, texId;

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
 
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Chapter 6 - Textures", NULL, NULL);
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
        
        // if you just renamed a random picture file
        // to *.png, it will not work, you have to convert it
        loadTextures();
        
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
    	GL20.glDisableVertexAttribArray(2);
    	GL30.glBindVertexArray(0);
    	GL30.glDeleteVertexArrays(vaoId);
    	
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	GL15.glDeleteBuffers(vboPosId);
    	GL15.glDeleteBuffers(vboColId);
    	GL15.glDeleteBuffers(vboTexId);
    	 
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboIndexId);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // unbind the texture
        GL13.glActiveTexture(0); // deactivate the texture unit
        glDeleteTextures(texId); // delete the texture
		
		GL20.glUseProgram(0);
		GL20.glDetachShader(pId, vsId);
		GL20.glDetachShader(pId, fsId);
		GL20.glDeleteShader(vsId);
		GL20.glDeleteShader(fsId);
		GL20.glDeleteProgram(pId);
	}
 
    private void defineSquare() {
    	float[] positions = {
	        -0.5f, 0.5f, 0.0f, 1.0f,	// 0 - top left
	        -0.5f, -0.5f, 0.0f, 1.0f,	// 1 - bottom left
	        0.5f, -0.5f, 0.0f, 1.0f,	// 2 - bottom right
	        0.5f, 0.5f, 0.0f, 1.0f };	// 3 - top right
        	
    	FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(positions.length);
    	positionBuffer.put(positions);
    	positionBuffer.flip();
    	
    	float[] colors = {
    		0.0f, 1.0f, 0.0f, 1.0f,		// 0 - green - top right
    		0.0f, 0.0f, 1.0f, 1.0f,		// 1 - blue - top right
    		1.0f, 1.0f, 1.0f, 1.0f,		// 2 - white - top right
    		1.0f, 0.0f, 0.0f, 1.0f };	// 3 - red - top right
    	
    	FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(colors.length);
    	colorBuffer.put(colors);
    	colorBuffer.flip();
    	
    	// if you put values between 0 and 1, it will clip
    	// the image according to those numbers
    	float[] texCoords = {
    		0.0f, 0.0f,	// 0 - top left
    		0.0f, 1.0f, 	// 1 - bottom left
    		1.0f, 1.0f, 	// 2 - bottom right
    		1.0f, 0.0f };	// 3 - top right
    	
    	FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(texCoords.length);
    	texCoordBuffer.put(texCoords);
    	texCoordBuffer.flip();
    	
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
    	GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	
    	vboColId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColId);
    	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
    	GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	
    	vboTexId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTexId);
    	GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer, GL15.GL_STATIC_DRAW);
    	GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 0, 0);
    	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    	
    	GL30.glBindVertexArray(0);
    	
    	vboIndexId = GL15.glGenBuffers();
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexId);
    	GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
    	GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    private void loadTextures() {
    	// GL_TEXTURE0 is going to be our diffuse texture
		texId = loadTexture("res/textures/c6/Bunny2D.PNG", GL13.GL_TEXTURE0);
	}
    
    private void initShaders() {
		vsId = loadShader("src/c6/shaders/vertex.sh", GL20.GL_VERTEX_SHADER);
		fsId = loadShader("src/c6/shaders/fragment.sh", GL20.GL_FRAGMENT_SHADER);
		
		pId = GL20.glCreateProgram();
		GL20.glAttachShader(pId, vsId);
		GL20.glAttachShader(pId, fsId);
		
		GL20.glBindAttribLocation(pId, 0, "in_Position");
		GL20.glBindAttribLocation(pId, 1, "in_Color");
		// added the texture coordinates here
		GL20.glBindAttribLocation(pId, 2, "in_TextureCoord");
		
		GL20.glLinkProgram(pId);
		GL20.glValidateProgram(pId);
	}

	private void renderSquare() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		// bind the approppriate texture unit,
		// currently we are only using diffuse texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId); // bind the texture
		
		GL20.glUseProgram(pId);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexId);
		 
		GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE, 0);
		 
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		GL20.glUseProgram(0);
		
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // unbind the texture
        GL13.glActiveTexture(0); // deactivate the texture unit
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
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
	
	public int loadShader(String filename, int type) {
	    StringBuilder shaderSource = new StringBuilder();
	    int shaderID = 0;
	     
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
	
	// a general function for loading textures
	// written by Mathias Verboven
	private int loadTexture(String filename, int textureUnit) {
        ByteBuffer buf = null;
        int tWidth = 0;
        int tHeight = 0;
         
        try {
            InputStream in = new FileInputStream(filename);
            PNGDecoder decoder = new PNGDecoder(in);
             
            // Get the width and height of the texture
            tWidth = decoder.getWidth();
            tHeight = decoder.getHeight();
             
            // Decode the PNG file in a ByteBuffer
            buf = ByteBuffer.allocateDirect(
                    4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
            buf.flip();
             
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
         
        // Create a new texture object in memory and bind it
        int texId = GL11.glGenTextures();
        GL13.glActiveTexture(textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
         
        // All RGB bytes are aligned to each other and each component is 1 byte
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
         
        // Upload the texture data and generate mip maps (for scaling)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, tWidth, tHeight, 0, 
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
         
        // Setup the ST coordinate system
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
         
        // Setup what to do when the texture has to be scaled
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 
                GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 
                GL11.GL_LINEAR_MIPMAP_LINEAR);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // unbind the texture
        GL13.glActiveTexture(0); // deactivate the texture unit
         
        return texId;
    }
 
    public static void main(String[] args) {
        new DisplayManager().run();
    }
    
}
