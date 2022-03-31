package main.engine.utility;

import java.io.File;

public class ResourcePaths {
	
	public static final class Engine {
		public static final String PROPERTIES = "resources|eng.properties".replace('|', File.separatorChar);
	}
	
	public static final class Fonts {
		public static final String FIRASANS_REGULAR = "resources|fonts|FiraSans-Regular.ttf".replace('|', File.separatorChar);
	}
	
	public static final class Models {
		public static final String BOBLAMP_DIR = "resources|models|bob".replace('|', File.separatorChar);
		public static final String BOBLAMP_MD5MESH = BOBLAMP_DIR + "|boblamp.md5mesh".replace('|', File.separatorChar);
		public static final String BUNNY_OBJ = "resources|models|bunny.obj".replace('|', File.separatorChar);
		public static final String CUBE_DIR = "resources|models|cube".replace('|', File.separatorChar);
		public static final String CUBE_OBJ = CUBE_DIR + "|cube.obj".replace('|', File.separatorChar);
		public static final String HOUSE_DIR = "resources|models|house".replace('|', File.separatorChar);
		public static final String HOUSE_OBJ = HOUSE_DIR +"|house.obj".replace('|', File.separatorChar);
		public static final String MCUBE_OBJ = "resources|models|cube.obj".replace('|', File.separatorChar);
		public static final String DOUBLE_QUAD_OBJ = "resources|models|double_quad.obj".replace('|', File.separatorChar);
		public static final String MONSTER_DIR = "resources|models|monster".replace('|', File.separatorChar);
		public static final String MONSTER_MD5MESH =  MONSTER_DIR + "|monster.md5mesh".replace('|', File.separatorChar);
		public static final String PARTICLE_OBJ = "resources|models|particle.obj".replace('|', File.separatorChar);
		public static final String PLANE_OBJ = "resources|models|plane.obj".replace('|', File.separatorChar);
		public static final String QUAD_OBJ = "resources|models|quad.obj".replace('|', File.separatorChar);
		public static final String SKYBOX_OBJ = "resources|models|skybox.obj".replace('|', File.separatorChar);
		public static final String BUFFER_PASS_DIR = "resources|models".replace('|', File.separatorChar);
		public static final String BUFFER_PASS_OBJ = "resources|models|buffer_pass_mesh.obj".replace('|', File.separatorChar);
		public static final String SPONZA_DIR = "resources|models|sponza".replace('|', File.separatorChar);
		public static final String SPONZA_GLTF = SPONZA_DIR + "|Sponza.gltf".replace('|', File.separatorChar);
	}
	
	public static final class Shaders {
		public static final class OpenGL {
			public static final String DEPTH_FRAGMENT = "resources|shaders|opengl|depth_fragment.fs".replace('|', File.separatorChar);
			public static final String DEPTH_VERTEX = "resources|shaders|opengl|depth_vertex.vs".replace('|', File.separatorChar);
			public static final String HUD_FRAGMENT = "resources|shaders|opengl|hud_fragment.fs".replace('|', File.separatorChar);
			public static final String HUD_VERTEX = "resources|shaders|opengl|hud_vertex.vs".replace('|', File.separatorChar);
			public static final String PARTICLES_FRAGMENT = "resources|shaders|opengl|particles_fragment.fs".replace('|', File.separatorChar);
			public static final String PARTICLES_VERTEX = "resources|shaders|opengl|particles_vertex.vs".replace('|', File.separatorChar);
			public static final String PINK_FRAGMENT = "resources|shaders|opengl|pink_fragment.fs".replace('|', File.separatorChar);
			public static final String PINK_VERTEX = "resources|shaders|opengl|pink_vertex.vs".replace('|', File.separatorChar);
			public static final String PORTAL_FRAGMENT = "resources|shaders|opengl|portal_fragment.fs".replace('|', File.separatorChar);
			public static final String PORTAL_VERTEX = "resources|shaders|opengl|portal_vertex.vs".replace('|', File.separatorChar);
			public static final String SB_FRAGMENT = "resources|shaders|opengl|sb_fragment.fs".replace('|', File.separatorChar);
			public static final String SB_VERTEX = "resources|shaders|opengl|sb_vertex.vs".replace('|', File.separatorChar);
			public static final String GBUFFER_FRAGMENT = "resources|shaders|opengl|gbuffer_fragment.fs".replace('|', File.separatorChar);
			public static final String GBUFFER_VERTEX = "resources|shaders|opengl|gbuffer_vertex.vs".replace('|', File.separatorChar);
			public static final String LIGHT_VERTEX = "resources|shaders|opengl|light_vertex.vs".replace('|', File.separatorChar);
			public static final String DIR_LIGHT_FRAGMENT = "resources|shaders|opengl|dir_light_fragment.fs".replace('|', File.separatorChar);
			public static final String POINT_LIGHT_FRAGMENT = "resources|shaders|opengl|point_light_fragment.fs".replace('|', File.separatorChar);
			public static final String FOG_FRAGMENT = "resources|shaders|opengl|fog_fragment.fs".replace('|', File.separatorChar);
		}
		
		public static final class Vulkan {
			public static final String ANIMATION_COMPUTE_GLSL = "resources|shaders|vulkan|animations_comp.glsl".replace('|', File.separatorChar);
			public static final String ANIMATION_COMPUTE_SPV = ANIMATION_COMPUTE_GLSL + ".spv";
			public static final String GEOMETRY_FRAGMENT_GLSL = "resources|shaders|vulkan|geometry_fragment.glsl".replace('|', File.separatorChar);
			public static final String GEOMETRY_FRAGMENT_SPV = GEOMETRY_FRAGMENT_GLSL + ".spv";
			public static final String GEOMETRY_VERTEX_GLSL = "resources|shaders|vulkan|geometry_vertex.glsl".replace('|', File.separatorChar);
			public static final String GEOMETRY_VERTEX_SPV = GEOMETRY_VERTEX_GLSL + ".spv";
			public static final String LIGHTING_FRAGMENT_GLSL = "resources|shaders|vulkan|lighting_fragment.glsl".replace('|', File.separatorChar);
			public static final String LIGHTING_FRAGMENT_SPV = LIGHTING_FRAGMENT_GLSL + ".spv";
			public static final String LIGHTING_VERTEX_GLSL = "resources|shaders|vulkan|lighting_vertex.glsl".replace('|', File.separatorChar);
			public static final String LIGHTING_VERTEX_SPV = LIGHTING_VERTEX_GLSL + ".spv";
			public static final String NUKLEAR_VERTEX_GLSL = "resources|shaders|vulkan|nuklear_vertex.glsl".replace('|', File.separatorChar);
			public static final String NUKLEAR_VERTEX_SPV = NUKLEAR_VERTEX_GLSL + ".spv";
			public static final String NUKLEAR_FRAGMENT_GLSL = "resources|shaders|vulkan|nuklear_fragment.glsl".replace('|', File.separatorChar);
			public static final String NUKLEAR_FRAGMENT_SPV = NUKLEAR_FRAGMENT_GLSL + ".spv";
			public static final String SHADOW_GEOMETRY_GLSL = "resources|shaders|vulkan|shadow_geometry.glsl".replace('|', File.separatorChar);
			public static final String SHADOW_GEOMETRY_SPV = SHADOW_GEOMETRY_GLSL + ".spv";
			public static final String SHADOW_VERTEX_GLSL = "resources|shaders|vulkan|shadow_vertex.glsl".replace('|', File.separatorChar);
			public static final String SHADOW_VERTEX_SPV = SHADOW_VERTEX_GLSL + ".spv";
		}
	}
	
	public static final class Sounds {
		public static final String BACKGROUND_OGG = "resources|sounds|background.ogg".replace('|', File.separatorChar);
		public static final String BEEP_OGG = "resources|sounds|beep.ogg".replace('|', File.separatorChar);
		public static final String FIRE_OGG = "resources|sounds|fire.ogg".replace('|', File.separatorChar);
	}
	
	public static final class Textures {
		public static final String TEXTURE_DIR = "resources|textures".replace('|', File.separatorChar);
		public static final String DEFAULT_TEXTURE = TEXTURE_DIR + "|default.png".replace('|', File.separatorChar);
	}
}
