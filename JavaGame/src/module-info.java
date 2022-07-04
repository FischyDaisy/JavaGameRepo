module JavaGame {
	requires java.desktop;
	//requires jdk.incubator.foreign;
	requires org.joml;
	requires org.joml.primitives;
	requires org.lwjgl;
	requires org.lwjgl.assimp;
	requires org.lwjgl.glfw;
	requires org.lwjgl.nuklear;
	requires org.lwjgl.openal;
	requires org.lwjgl.opengl;
	requires org.lwjgl.shaderc;
	requires org.lwjgl.stb;
	requires org.lwjgl.vma;
	requires org.lwjgl.vulkan;
	requires org.tinylog.api;
	requires crab.JNewton;
	
	exports main.game;
}