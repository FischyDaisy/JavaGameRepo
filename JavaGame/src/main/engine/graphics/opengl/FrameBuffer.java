package main.engine.graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

import java.nio.ByteBuffer;

public class FrameBuffer {
	
	public static final int FBO_SIZE = 2048;
	
	//private final int texId;
	
	private final Texture text;
	
	private final int fbo;
	
	private final int renderBuf;
	
	public FrameBuffer() throws Exception {
		// Generate Texture
		text = new Texture(FBO_SIZE, FBO_SIZE, GL_RGB);
		/*
		texId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texId);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, FBO_SIZE, FBO_SIZE, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		*/
		// Generate FBO
		fbo = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, text.getId(), 0);
		
		// Generate Render Buffer
		renderBuf = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, renderBuf);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, FBO_SIZE, FBO_SIZE);
		
		//Bind Render Buffer
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuf);
		
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Failed to create FrameBuffer");
		}
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void bindTexture() {
		text.bind();
	}
	
	public void bindFrameBuffer() {
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	}
	
	public Texture getTexture() {
		return text;
	}
	
	public int getFrameBufferId() {
		return fbo;
	}
	
	public int getRenderBufferId() {
		return renderBuf;
	}
	
	protected void initRender() {
		
	}
	
	protected void endRender() {
		
	}
	
	public void cleanup() {
		glDeleteFramebuffers(fbo);
		glDeleteRenderbuffers(renderBuf);
		text.cleanup();
	}
}
