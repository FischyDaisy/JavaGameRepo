package main.engine.graphics.ui;

import org.lwjgl.nuklear.NkAllocator;
import org.lwjgl.nuklear.NkDrawVertexLayoutElement;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.NK_FORMAT_COUNT;
import static org.lwjgl.system.MemoryUtil.nmemAllocChecked;
import static org.lwjgl.system.MemoryUtil.nmemFree;

public class UIManager {
    public static final long MAX_ELEMENTS = 20;
    public static final long BUFFER_INITIAL_SIZE = 4 * 1024;
    public static final long MAX_VERTEX_BUFFER  = 512 * 1024;
    public static final long MAX_INDICES_BUFFER = 128 * 1024;
    public static final long NULL_TEXTURE_ID = 0;
    public static final long FONT_TEXTURE_ID = 1;
    private static final NkAllocator ALLOCATOR;

    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;

    static {
        ALLOCATOR = NkAllocator.create()
                .alloc((handle, old, size) -> nmemAllocChecked(size))
                .mfree((handle, ptr) -> nmemFree(ptr));

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
                .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                .flip();
    }
}
