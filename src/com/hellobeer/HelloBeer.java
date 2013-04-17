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

/**
 * Main class
 */
public class HelloBeer
{
	public static void main(String[] args) throws IOException,
			DocumentException, IllegalArgumentException,
			IllegalAccessException, SecurityException, NoSuchFieldException
	{
		// Command line parameters:
		//		[0]		[source-file-name].pdf
		//		[1]		[output-file-name].pdf
		//		[2-n]	[beer name (replace spaces with underscores)]
		
		PdfReader reader = new PdfReader(args[0]);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(args[1]));
		stamper.setFormFlattening(true);
		stamper.setFreeTextFlattening(true);
		stamper.getWriter().setCompressionLevel(0);
		
		ArrayList<Beer> beerList = new ArrayList<Beer>();
		for (int i = 2; i < args.length; i++)
		{
			// In order to pass multiple-word beer names as command parameters, spaces are replaced with underscores.  Reverse that. 
			beerList.add( new Beer(args[i].replace('_', ' ')) );
		}
		
		// Process page 1 and 2...
		for (int page = 1; page <= reader.getNumberOfPages(); page++)
		{
			// PdfStamper is responsible for redlining matches
			PdfContentByte over = stamper.getOverContent(page);
			over.saveState();
			over.setRGBColorStroke(0xFF, 0x00, 0x00);
			over.setLineWidth(1f);
			
			FindBeerStrategy strat = new FindBeerStrategy();
			
			// PdfTextExtractor parses the PDF according to FindBeerStrategy.
			// Line data stored in strat.entries
			PdfTextExtractor.getTextFromPage(reader, page, strat);
			
			String past = "";			// Previous line data that matches a beer name pattern
			String text = "";			// Current line data
			String searchStr = "";		// Current beer name
			String bold = "";			// Last identified brewery name, this may be written above a list of beer names
			boolean draw = false;
			float startX = 0;
			float startY = 0;
			float endX = 0;
			boolean skip = false;
			boolean trimLine = false;
			
			// For each beer in beerList, iterate through the lines in strat.entries and search for matches.
			for (Beer beer : beerList)
			{
				// To simplify search, all strings are upcased and special characters are removed.
				searchStr = beer.name.toUpperCase().replaceAll("[^A-Z0-9]", "");
				
				skip = false;
				
				for (Line line : strat.entries)
				{	
					draw = false;
					
					// Determining boldness from text width is unreliable, so to begin with, mark lines without prices as bold.
					if (line.getText().indexOf('$') == -1)
					{
						line.isBold = true;
					}

					// To simplify search, all strings are upcased and special characters are removed.
					text = line.getText().toUpperCase().replaceAll("[^A-Z0-9]", "");
					
					// Text blocks are read in and grouped by page section, so apply search params here:
					//		skip		if true, skips block entirely
					//		trimLine	if true, allows for multiple menu items on a single line (see SPECIALS box)
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
					
					// Skip over items in unchecked sections
					if (skip)
						continue;

					/// MATCHING SECTION
					
					// If the current line exactly equals the search string, MATCH
					if (text.contains(searchStr))
					{
						draw = true;
						past = "";
						over.newPath();
						
						startX = line.getStartX(trimLine, searchStr);
						startY = line.y;
						over.moveTo(startX, startY + 2);
					}
					// Else if the search string begins with the brewery name
					else if (!bold.isEmpty() && searchStr.startsWith(bold))
					{
						// If brewery name + current line contains the search string, MATCH
						if ((bold+text).contains(searchStr))
						{
							draw = true;
							past = "";
							over.newPath();
							
							startX = line.getStartX(trimLine, searchStr);
							startY = line.y;
							over.moveTo(startX, startY + 2);
						}
						// Else if brewery name + previous line + current line contains the search string, MATCH
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
						// Else store the current line as previous line and reset
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
					// Else if previous line + current line contains the search string, MATCH
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
					// Throw out empty string here
					else if (text.isEmpty())	{}
					// Else store the current line as previous line and reset
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
					
					// Draw the actual red line and mark the beer as found
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
		
		// For double-checking, page 3 lists beers not found
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

		// Close stamper to save.
		stamper.close();
	}
}

