/*
 * Copyright 2011-2015 L2EMU UNIQUE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.l2emuproject.proxy.ui.savormix.io;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Map;

import javax.imageio.ImageIO;

import javolution.util.FastMap;

import net.l2emuproject.proxy.ui.savormix.io.base.IOConstants;
import net.l2emuproject.util.logging.L2Logger;

/**
 * Manages pictures used in the HTML packet view.
 * 
 * @author savormix
 */
public class ImageUrlUtils implements IOConstants
{
	private static final int LEGACY_IO_RETRY_COUNT = 3;
	static final L2Logger LOG = L2Logger.getLogger(ImageUrlUtils.class);
	
	final Path _directory;
	final Map<URL, Path> _images;
	
	ImageUrlUtils()
	{
		_directory = APPLICATION_DIRECTORY.resolve("temp");
		_images = new FastMap<URL, Path>().setShared(true);
		net.l2emuproject.lang.management.ShutdownManager.addShutdownHook(new Runnable()
		{
			@Override
			public void run()
			{
				LOG.info("Cleaning linked image cache...");
				
				for (final Path p : _images.values())
				{
					try
					{
						Files.deleteIfExists(p);
					}
					catch (IOException e)
					{
						LOG.warn("Cannot remove " + p.getFileName(), e);
					}
				}
				
				LOG.info("Session cache cleared.");
				
				try
				{
					Files.deleteIfExists(_directory);
					
					LOG.info("Cache removed from file system.");
				}
				catch (DirectoryNotEmptyException e)
				{
					LOG.info("Cache contains elements from other sessions; purging cache.");
					try
					{
						Files.walkFileTree(_directory, new Purgatory());
					}
					catch (IOException e2)
					{
						LOG.warn("Purge failed, are you running multiple instances with the same user?", e2);
					}
				}
				catch (IOException e)
				{
					LOG.warn("Could not remove cache directory.", e);
				}
			}
		});
	}
	
	/**
	 * Saves the given image to disk and returns an URL that can safely be used in java's own HTML rendering components.
	 * 
	 * @param prefix a prefix to give to the image file
	 * @param image an image
	 * @return saved file URL
	 */
	public URL toUrl(String prefix, RenderedImage image)
	{
		// sometimes, the created temp file cannot be reliably opened for reading and writing by the old I/O
		for (int i = 0; i < LEGACY_IO_RETRY_COUNT; ++i)
		{
			try
			{
				Files.createDirectories(_directory);
				
				final Path tmp = Files.createTempFile(_directory, prefix, ".png");
				ImageIO.write(image, "png", tmp.toFile());
				
				final URL url = tmp.toUri().toURL();
				_images.put(url, tmp);
				return url;
			}
			catch (IOException e)
			{
				LOG.error("Cannot write an image to cache.", e);
				return null;
			}
			catch (NullPointerException e)
			{
				// typically access denied for old I/O
				continue;
			}
		}
		LOG.error("Cannot write an image to temp cache.");
		return null;
	}
	
	/**
	 * Removes given image URLs from cache and deletes the associated files.
	 * 
	 * @param urls cached image URLs
	 */
	public void release(Collection<URL> urls)
	{
		for (final URL url : urls)
		{
			final Path p = _images.remove(url);
			if (p == null)
				continue;
			
			try
			{
				Files.deleteIfExists(p);
			}
			catch (IOException e)
			{
				LOG.warn("Cannot remove an image from cache.", e);
			}
		}
	}
	
	/**
	 * Returns a singleton instance of this type.
	 * 
	 * @return an instance of this class
	 */
	public static final ImageUrlUtils getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final ImageUrlUtils INSTANCE = new ImageUrlUtils();
	}
	
	static final class Purgatory implements FileVisitor<Path>
	{
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
		{
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			Files.deleteIfExists(file);
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
		{
			Files.deleteIfExists(file);
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
		{
			Files.deleteIfExists(dir);
			return FileVisitResult.CONTINUE;
		}
	}
}
