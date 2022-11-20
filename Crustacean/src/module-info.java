module Crustacean {
    requires java.desktop;
    requires org.joml;
    requires org.joml.primitives;
    requires org.lwjgl;
    requires org.lwjgl.natives;
    requires org.lwjgl.assimp;
    requires org.lwjgl.assimp.natives;
    requires org.lwjgl.glfw;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.nuklear;
    requires org.lwjgl.nuklear.natives;
    requires org.lwjgl.openal;
    requires org.lwjgl.openal.natives;
    requires org.lwjgl.shaderc;
    requires org.lwjgl.shaderc.natives;
    requires org.lwjgl.stb;
    requires org.lwjgl.stb.natives;
    requires org.lwjgl.vma;
    requires org.lwjgl.vma.natives;
    requires org.lwjgl.vulkan;
    requires org.tinylog.api;
    requires crab.jnewton;
    requires crab.jnewton.windows_x64;
    requires dev.dominion.ecs.api;

    exports main.game;
}