package com.hellobeer;

import java.util.ArrayList;

public class Line
{
	public class Block
	{
		String text;
		float startX;
		float endX;
	}
	
	public float y;
	boolean isBold = false;
	public ArrayList<Block> blocks = new ArrayList<Block>();
	
	public String getText()
	{
		StringBuilder stb = new StringBuilder();
		for (Block block : blocks)
		{
			stb.append(block.text);
		}
		return stb.toString();
	}
	
	public float getStartX(boolean trim)
	{
		return getStartX(trim, "");
	}
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
	
	public float getEndX(boolean trim)
	{
		return getEndX(trim, "");
	}
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