package dev.xfj;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;

public class Image {
    private int width;
    private int height;
    private int rendererId;
    private ImageFormat format;
    private String filePath;

    public enum ImageFormat
    {
        None,
        RGBA,
        RGBA32F
    }

    public Image(String filePath) {
        this.filePath = filePath;

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);


        ByteBuffer data = null;

        Path path = Path.of(filePath);

        if (STBImage.stbi_is_hdr(filePath)) {
            data = STBImage.stbi_load(path.normalize().toString(), width, height, channels, 4);
            format = ImageFormat.RGBA32F;
        } else {
            data = STBImage.stbi_load(path.normalize().toString(), width, height, channels, 4);
            format = ImageFormat.RGBA;
        }

        this.width = width.get(0);
        this.height = height.get(0);

        setData(data, channels);
        STBImage.stbi_image_free(data);

    }

    public Image(int width, int height, ImageFormat format) {
        this.width = width;
        this.height = height;
        this.format = format;
    }

    public void setData(ByteBuffer data, IntBuffer channels) {
        int internalFormat = 0;
        int dataFormat = 0;

        if (channels.get(0) == 4) {
            internalFormat = GL_RGBA8;
            dataFormat = GL_RGBA;
        } else if (channels.get(0) == 3) {
            internalFormat = GL45.GL_RGB8;
            dataFormat = GL45.GL_RGB;
        }
        //Some sort of exception HZ_CORE_ASSERT(internalFormat & dataFormat, "Format not supported!");

        this.rendererId = GL45.glCreateTextures(GL45.GL_TEXTURE_2D);
        GL45.glTextureStorage2D(this.rendererId, 1, internalFormat, this.width, this.height);

        GL45.glTextureParameteri(this.rendererId, GL45.GL_TEXTURE_MIN_FILTER, GL45.GL_LINEAR);
        GL45.glTextureParameteri(this.rendererId, GL45.GL_TEXTURE_MAG_FILTER, GL45.GL_LINEAR);

        GL45.glTextureParameteri(this.rendererId, GL45.GL_TEXTURE_WRAP_S, GL45.GL_REPEAT);
        GL45.glTextureParameteri(this.rendererId, GL45.GL_TEXTURE_WRAP_T, GL45.GL_REPEAT);

        GL45.glTextureSubImage2D(this.rendererId, 0, 0, 0, this.width, this.height, dataFormat, GL45.GL_UNSIGNED_BYTE, data);
    }

    public void resize(int width, int height) {

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
