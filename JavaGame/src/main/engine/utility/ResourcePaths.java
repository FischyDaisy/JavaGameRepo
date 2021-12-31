package main.engine.utility;

public class ResourcePaths {
	
	public static final class Engine {
		public static final String PROPERTIES = "resources\\eng.properties";
	}
	
	public static final class Fonts {
		public static final String FIRASANS_REGULAR = "resources\\fonts\\FiraSans-Regular.ttf";
	}
	
	public static final class Models {
		public static final String BOBLAMP_MD5ANIM = "resources\\models\\boblamp.md5anim";
		public static final String BOBLAMP_MD5MESH = "resources\\models\\boblamp.md5mesh";
		public static final String BUNNY_OBJ = "resources\\models\\bunny.obj";
		public static final String CUBE_DIR = "resources\\models\\cube";
		public static final String CUBE_OBJ = CUBE_DIR + "\\cube.obj";
		public static final String HOUSE_DIR = "resources\\models\\house";
		public static final String HOUSE_OBJ = HOUSE_DIR +"\\house.obj";
		public static final String MCUBE_OBJ = "resources\\models\\cube.obj";
		public static final String DOUBLE_QUAD_OBJ = "resources\\models\\double_quad.obj";
		public static final String MONSTER_MD5ANIM = "resources\\models\\monster.md5anim";
		public static final String MONSTER_MD5MESH = "resources\\models\\monster.md5mesh";
		public static final String PARTICLE_OBJ = "resources\\models\\particle.obj";
		public static final String PLANE_OBJ = "resources\\models\\plane.obj";
		public static final String QUAD_OBJ = "resources\\models\\quad.obj";
		public static final String SKYBOX_OBJ = "resources\\models\\skybox.obj";
		public static final String BUFFER_PASS_DIR = "resources\\models";
		public static final String BUFFER_PASS_OBJ = "resources\\models\\buffer_pass_mesh.obj";
	}
	
	public static final class Shaders {
		public static final class OpenGL {
			public static final String DEPTH_FRAGMENT = "resources\\shaders\\opengl\\depth_fragment.fs";
			public static final String DEPTH_VERTEX = "resources\\shaders\\opengl\\depth_vertex.vs";
			public static final String HUD_FRAGMENT = "resources\\shaders\\opengl\\hud_fragment.fs";
			public static final String HUD_VERTEX = "resources\\shaders\\opengl\\hud_vertex.vs";
			public static final String PARTICLES_FRAGMENT = "resources\\shaders\\opengl\\particles_fragment.fs";
			public static final String PARTICLES_VERTEX = "resources\\shaders\\opengl\\particles_vertex.vs";
			public static final String PINK_FRAGMENT = "resources\\shaders\\opengl\\pink_fragment.fs";
			public static final String PINK_VERTEX = "resources\\shaders\\opengl\\pink_vertex.vs";
			public static final String PORTAL_FRAGMENT = "resources\\shaders\\opengl\\portal_fragment.fs";
			public static final String PORTAL_VERTEX = "resources\\shaders\\opengl\\portal_vertex.vs";
			public static final String SB_FRAGMENT = "resources\\shaders\\opengl\\sb_fragment.fs";
			public static final String SB_VERTEX = "resources\\shaders\\opengl\\sb_vertex.vs";
			public static final String GBUFFER_FRAGMENT = "resources\\shaders\\opengl\\gbuffer_fragment.fs";
			public static final String GBUFFER_VERTEX = "resources\\shaders\\opengl\\gbuffer_vertex.vs";
			public static final String LIGHT_VERTEX = "resources\\shaders\\opengl\\light_vertex.vs";
			public static final String DIR_LIGHT_FRAGMENT = "resources\\shaders\\opengl\\dir_light_fragment.fs";
			public static final String POINT_LIGHT_FRAGMENT = "resources\\shaders\\opengl\\point_light_fragment.fs";
			public static final String FOG_FRAGMENT = "resources\\shaders\\opengl\\fog_fragment.fs";
		}
		
		public static final class Vulkan {
			public static final String FWD_FRAGMENT_GLSL = "resources\\shaders\\vulkan\\fwd_fragment.glsl";
			public static final String FWD_FRAGMENT_SPV = FWD_FRAGMENT_GLSL + ".spv";
			public static final String FWD_VERTEX_GLSL = "resources\\shaders\\vulkan\\fwd_vertex.glsl";
			public static final String FWD_VERTEX_SPV = FWD_VERTEX_GLSL + ".spv";
		}
	}
	
	public static final class Sounds {
		public static final String BACKGROUND_OGG = "resources\\sounds\\background.ogg";
		public static final String BEEP_OGG = "resources\\sounds\\beep.ogg";
		public static final String FIRE_OGG = "resources\\sounds\\fire.ogg";
	}
	
	public static final class Textures {
		public static final String TEXTURE_DIR = "resources\\textures";
	}
}
