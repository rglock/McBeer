package com.hellobeer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class HelloBeer
{
	public static void main(String[] args) throws IOException,
			DocumentException, IllegalArgumentException,
			IllegalAccessException, SecurityException, NoSuchFieldException
	{		
		PdfReader reader = new PdfReader(args[0]);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(args[1]));
		stamper.setFormFlattening(true);
		stamper.setFreeTextFlattening(true);
		stamper.getWriter().setCompressionLevel(0);
		
		ArrayList<Beer> beerList = new ArrayList<Beer>();
		for (int i = 2; i < args.length; i++)
		{
			beerList.add( new Beer(args[i].replace('_', ' ')) );
		}
		
		for (int page = 1; page <= reader.getNumberOfPages(); page++)
		{
			PdfContentByte over = stamper.getOverContent(page);
			over.saveState();
			over.setRGBColorStroke(0xFF, 0x00, 0x00);
			over.setLineWidth(1f);
			
			FindBeerStrategy strat = new FindBeerStrategy();
			
			PdfTextExtractor.getTextFromPage(reader, page, strat);
			
			String past = "";
			String text = "";
			String searchStr = "";
			String bold = "";
			boolean draw = false;
			float startX = 0;
			float startY = 0;
			float endX = 0;
			boolean skip = false;
			boolean trimLine = false;
			
			for (Beer beer : beerList)
			{
				searchStr = beer.name.toUpperCase().replaceAll("[^A-Z0-9]", "");
				
				skip = false;
				
				for (Line line : strat.entries)
				{	
					draw = false;
					
					if (line.getText().indexOf('$') == -1)
					{
						line.isBold = true;
					}

					text = line.getText().toUpperCase().replaceAll("[^A-Z0-9]", "");
					
					if (text.compareTo("DRAUGHT") == 0)
					{
						skip = false;
						trimLine = false;
					}
					else if (text.compareTo("BEERMIXES") == 0)
					{
						skip = true;
						trimLine = false;
					}
					else if (text.compareTo("SPECIALS") == 0)
					{
						skip = false;
						trimLine = true;
					}
					else if (text.compareTo("FLIGHTS") == 0)
					{
						skip = true;
						trimLine = false;
					}
					else if (text.compareTo("PINTNIGHTS") == 0)
					{
						skip = true;
						trimLine = false;
					}
					else if (text.compareTo("FOLLOWERSFRIDAY") == 0)
					{
						skip = true;
						trimLine = false;
					}
					else if (text.compareTo("DAILYEVENTS") == 0)
					{
						skip = true;
						trimLine = false;
					}
					else if (text.compareTo("BOTTLED") == 0)
					{
						skip = false;
						trimLine = false;
					}
					
					if (skip)
						continue;

					if (text.contains(searchStr))
					{
						draw = true;
						past = "";
						over.newPath();
						
						startX = line.getStartX(trimLine, searchStr);
						startY = line.y;
						over.moveTo(startX, startY + 2);
					}
					else if (!bold.isEmpty() && searchStr.startsWith(bold))
					{
						if ((bold+text).contains(searchStr))
						{
							draw = true;
							past = "";
							over.newPath();
							
							startX = line.getStartX(trimLine, searchStr);
							startY = line.y;
							over.moveTo(startX, startY + 2);
						}
						else if (!past.isEmpty() && searchStr.startsWith(bold+past))
						{
							past += text;
							
							if (startY == line.y)
							{
								startX = line.getStartX(trimLine, searchStr);
								startY = line.y;
								over.lineTo(startX, startY + 2);
							}
							else
							{
								startX = line.getStartX(trimLine, searchStr);
								startY = line.y;
								over.lineTo(startX, startY + 2);
							}
							
							if ((bold+past).contains(searchStr))
							{
								draw = true;
								past = "";
							}
						}
						else
						{
							past = text;
							over.newPath();
							
							startX = line.getStartX(trimLine, searchStr);
							startY = line.y;
							over.lineTo(startX, startY + 2);
							
							if (line.isBold)
								bold = text;
						}
					}
					else if (!past.isEmpty() && searchStr.startsWith(past))
					{
						past += text;
						
						if (startY == line.y)
						{
							startX = line.getStartX(trimLine, searchStr);
							startY = line.y;
							over.lineTo(startX, startY + 2);
						}
						else
						{
							startX = line.getStartX(trimLine, searchStr);
							startY = line.y;
							over.lineTo(startX, startY + 2);
						}
						
						if (past.contains(searchStr))
						{
							draw = true;
							past = "";
						}
					}
					else if (text.isEmpty())	{}
					else
					{
						past = text;
						over.newPath();
						
						startX = line.getStartX(trimLine, searchStr);
						startY = line.y;
						over.lineTo(startX, startY + 2);
						
						if (line.isBold)
							bold = text;
					}
					
					if (draw)
					{
						endX = line.getEndX(trimLine, searchStr);
						over.lineTo(endX, startY + 2);
						over.stroke();
						
						beer.found = true;
					}
				}
			}
			
			over.restoreState();
		}
		
		stamper.insertPage(3, reader.getPageSize(1));
		
		PdfContentByte over = stamper.getOverContent(3);
		BaseFont NormalFont = BaseFont.createFont("c:\\windows\\fonts\\PER_____.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
		over.setFontAndSize(NormalFont, 12);
		over.beginText();
		over.moveText(30, reader.getPageSize(3).getHeight() - 30);
		over.moveTextWithLeading(0, -14);

		Collections.sort(beerList);
		for (Beer beer : beerList)
		{
			if (!beer.found)
			{
				over.setColorFill(BaseColor.RED); 
				over.showText(beer.name);
				over.setColorFill(BaseColor.BLACK); 
				over.showText(" not found.");
				over.newlineText();
			}
		}
		
		over.endText();

		stamper.close();
	}
}

