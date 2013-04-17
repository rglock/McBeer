package com.hellobeer;

import java.util.ArrayList;

/**
 * Represents all the text of a single (y-value) line.
 */
public class Line
{
	/**
	 * Represents a string on a single line.
	 * May be as long as the whole width of a line or as short as a space character. 
	 */
	public class Block
	{
		String text;
		float startX;
		float endX;
	}
	
	public float y;
	boolean isBold = false;
	public ArrayList<Block> blocks = new ArrayList<Block>();
	
	/**
	 * Returns sum of strings in blocks. 
	 * @return The text contained in this line.
	 */
	public String getText()
	{
		StringBuilder stb = new StringBuilder();
		for (Block block : blocks)
		{
			stb.append(block.text);
		}
		return stb.toString();
	}
	
	/**
	 * 
	 * @param trim
	 * @param text
	 * @return
	 */
	public float getStartX(boolean trim, String text)
	{
		if (blocks.size() > 0)
		{
			if (trim)
			{
				for (Block block : blocks)
				{
					String blockText = block.text.toUpperCase().replaceAll("[^A-Z0-9]", "");
					if (!blockText.isEmpty() && text.startsWith(blockText))
						return block.startX;
				}
			}
			
			return blocks.get(0).startX;
		}
		
		return 0;
	}
	
	/**
	 * 
	 * @param trim
	 * @param text
	 * @return
	 */
	public float getEndX(boolean trim, String text)
	{
		if (blocks.size() > 0)
		{
			if (trim)
			{
				for (int i = blocks.size() - 1; i >= 0; i--)
				{
					Block block = blocks.get(i);
					String blockText = block.text.toUpperCase().replaceAll("[^A-Z0-9]", "");
					if (!blockText.isEmpty() && text.endsWith(blockText))
					{
						return block.endX;
					}
				}
			}
			
			return blocks.get(blocks.size() - 1).endX;
		}
		
		return 0;
	}
}