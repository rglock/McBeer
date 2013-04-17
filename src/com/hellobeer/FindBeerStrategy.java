package com.hellobeer;

import java.util.ArrayList;

import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class FindBeerStrategy extends LocationTextExtractionStrategy
{
	public ArrayList<Line> entries = new ArrayList<Line>();
	
	@Override
	public void renderText(TextRenderInfo renderInfo)
	{
		super.renderText(renderInfo);
		
		float startX = renderInfo.getBaseline().getStartPoint().get(0);
		float endX = renderInfo.getBaseline().getEndPoint().get(0);
		float Y = renderInfo.getBaseline().getStartPoint().get(1);
		
		int lastEntry = entries.size() - 1;
		
		if (entries.size() > 0 && Y == entries.get(lastEntry).y)
		{
			Line.Block block = entries.get(lastEntry).new Block();
			block.text = renderInfo.getText();
			block.startX = startX;
			block.endX = endX;
			entries.get(lastEntry).blocks.add(block);
		}
		else
		{
			Line line = new Line();
			Line.Block block = line.new Block();
			block.text = renderInfo.getText();
			block.startX = startX;
			block.endX = endX;
			line.y = Y;
			line.blocks.add(block);
			entries.add(line);
		}
	}	
}
