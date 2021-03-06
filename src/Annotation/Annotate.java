/*
 * @author: Nikola Milosevic
 * @affiliation: University of Manchester, School of Computer science
 * 
 */
package Annotation;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Main.Word;
import Utils.Utilities;
import ValueParser.ValueItem;
import ValueParser.ValueItem.ValueType;
import ValueParser.ValueParser;
import tablInEx.Article;
import tablInEx.Cell;
import tablInEx.TablInExMain;
import tablInEx.Table;
import tablInEx.Table.StructureType;

// TODO: Auto-generated Javadoc
/**
 * The Class Annotate.
 */
public class Annotate {

	/**
	 * Annotate article with annotation schema.
	 *
	 * @param a the read article
	 */
	public void AnnotateArticle(Article a)
	{
		try {
			Utilities.MakeDirectory("Annotation");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("Article");
			doc.appendChild(rootElement);
			Element pmcid = doc.createElement("PMCID");
			pmcid.setTextContent(a.getPmc());
			rootElement.appendChild(pmcid);
			
			Element pcid = doc.createElement("PMID");
			pcid.setTextContent(a.getPmid());
			rootElement.appendChild(pcid);
			
			Element pissn = doc.createElement("p-issn");
			pissn.setTextContent(a.getPissn());
			rootElement.appendChild(pissn);
			
			Element eissn = doc.createElement("e-issn");
			eissn.setTextContent(a.getEissn());
			rootElement.appendChild(eissn);
			
			Element title = doc.createElement("Title");
			title.setTextContent(a.getTitle());
			rootElement.appendChild(title);
			
			Element authors = doc.createElement("Authors");
			for(int i = 0; i<a.getAuthors().size();i++){
				Element author = doc.createElement("Author");
				Element authorName = doc.createElement("AuthorName");
				authorName.setTextContent(a.getAuthors().get(i).name);
				author.appendChild(authorName);
				for(int j=0;j<a.getAuthors().get(i).affiliation.size();j++){
				Element AuthorAffiliation = doc.createElement("AuthorAffiliation");
				AuthorAffiliation.setTextContent(a.getAuthors().get(i).affiliation.get(j));
				author.appendChild(AuthorAffiliation);
				}
				Element authorEmail = doc.createElement("AuthorEmail");
				authorEmail.setTextContent(a.getAuthors().get(i).email);
				author.appendChild(authorEmail);
				authors.appendChild(author);
			}
			rootElement.appendChild(authors);
					
			Element keywords = doc.createElement("KeyWords");
			for(int i = 0; i<a.getKeywords().length;i++){
				Element keyword = doc.createElement("KeyWord");
				keyword.setTextContent(a.getKeywords()[i]);
				keywords.appendChild(keyword);
			}
			rootElement.appendChild(keywords);

			Element publisher = doc.createElement("JournalInformation");
			Element publisherName = doc.createElement("PublisherName");
			publisherName.setTextContent(a.getPublisher_name());
			publisher.appendChild(publisherName);
			
			Element publisherLoc = doc.createElement("PublisherLocation");
			publisherLoc.setTextContent(a.getPublisher_loc());
			publisher.appendChild(publisherLoc);
			rootElement.appendChild(publisher);
			
			Element venue = doc.createElement("Venue");
			venue.setTextContent(a.getVenue());
			publisher.appendChild(venue);
			
			Element journal = doc.createElement("journal");
			journal.setTextContent(a.getJournal_name());
			publisher.appendChild(journal);
			
			Element abstractEl = doc.createElement("Abstract");
			abstractEl.setTextContent(a.getAbstract());
			rootElement.appendChild(abstractEl);
			
			Table[] tables = a.getTables();
			Element tablesEl = doc.createElement("Tables");
			rootElement.appendChild(tablesEl);
			for(int i = 0;i<tables.length;i++)
			{
				Element tableEl = doc.createElement("Table");
				tablesEl.appendChild(tableEl);
				Table table = tables[i];
				
				Element TabOrder = doc.createElement("TableOrder");
				TabOrder.setTextContent(table.getTable_title());
				tableEl.appendChild(TabOrder);
				
				Element TabCaption = doc.createElement("TableCaption");
				TabCaption.setTextContent(table.getTable_caption());
				tableEl.appendChild(TabCaption);
				
				Element TabFooter = doc.createElement("TableFooter");
				TabFooter.setTextContent(table.getTable_footer());
				tableEl.appendChild(TabFooter);
				
				Element TabStructure = doc.createElement("TableStructureType");
				if(table.getTableStructureType()==null)
				{
					table.setTableStructureType(StructureType.NULL);
				}
				TabStructure.setTextContent(table.getTableStructureType().toString());
				tableEl.appendChild(TabStructure);
				
				Element TabPragmatic = doc.createElement("TablePragmaticClass");
				TabPragmatic.setTextContent(table.PragmaticClass);
				tableEl.appendChild(TabPragmatic);
				
				Element TabHasXML = doc.createElement("TabHasXML");
				if(table.isNoXMLTable())
					TabHasXML.setTextContent("no");
				else
					TabHasXML.setTextContent("yes");
				
				tableEl.appendChild(TabHasXML);
				Element CellsEl = doc.createElement("Cells");
				tableEl.appendChild(CellsEl);
				
				Cell[][] cells = table.original_cells;
				if(cells!=null){
				for(int j = 0;j<cells.length;j++)
				{
					for(int k = 0;k<cells[j].length;k++)
					{
						Element CellEl = doc.createElement("Cell");
						CellsEl.appendChild(CellEl);
						Element CellID = doc.createElement("CellID");
						String cellIDStr = ""+j+"."+k;
						CellID.setTextContent(cellIDStr);
						cells[j][k].CellId = cellIDStr;
						CellEl.appendChild(CellID);
						
						if(cells[j][k].getSuperRowIndex()!=null&&!cells[j][k].getSuperRowIndex().equals(""))
						{
							Element SuperRowRef = doc.createElement("SuperRowRef");
							SuperRowRef.setTextContent(cells[j][k].getSuperRowIndex());
							CellEl.appendChild(SuperRowRef);
						}
						
						Element CellValue = doc.createElement("CellValue");
						CellValue.setTextContent(cells[j][k].getCell_content());
						CellEl.appendChild(CellValue);
						
						
						String valueToParse = cells[j][k].getCell_content();
						LinkedList<ValueItem> valueTags = TablInExMain.vp.parseValue(valueToParse);
						Element CellSemantics = doc.createElement("CellSemantics");
						CellEl.appendChild(CellSemantics);
						for(int p = 0; p<valueTags.size();p++)
						{
							Element CellValueSemantics = doc.createElement("CellValueSem");
							CellValueSemantics.setAttribute("Type", valueTags.get(p).type.toString());
							CellValueSemantics.setAttribute("Start", valueTags.get(p).start_position+"");
							CellValueSemantics.setAttribute("End", valueTags.get(p).end_position+"");
							CellValueSemantics.setTextContent(valueTags.get(p).value);
							CellSemantics.appendChild(CellValueSemantics);
						}
						//annotating by MARVIN
						//TODO: ADD This bit when DBPedia is installed locally
						int mathTypeIndex = valueToParse.indexOf("MathType@");
						if(mathTypeIndex>0)
						{
							valueToParse = valueToParse.substring(0, mathTypeIndex);
						}
						LinkedList<Word> words = TablInExMain.marvin.annotateWordNetOnly(valueToParse);
						if(words!=null){
						for(int p = 0;p<words.size();p++){
							Element CellValueSemantics = doc.createElement("CellValueSem");
							CellValueSemantics.setAttribute("Type", ValueType.TEXT.toString());
							CellValueSemantics.setAttribute("Start", words.get(p).starting+"");
							CellValueSemantics.setAttribute("End", words.get(p).ending+"");
							CellValueSemantics.setTextContent(words.get(p).word);
							CellSemantics.appendChild(CellValueSemantics);
							for(int s = 0;s<words.get(p).wordmeanings.size();s++){
								Element Meaning = doc.createElement("ValueMeaning");
								Meaning.setAttribute("Source", words.get(p).wordmeanings.get(s).Source);
								Meaning.setAttribute("ID", words.get(p).wordmeanings.get(s).id);
								Meaning.setAttribute("URL", words.get(p).wordmeanings.get(s).URL);
								Meaning.setAttribute("Start", words.get(p).wordmeanings.get(s).startAt+"");
								Meaning.setAttribute("End", words.get(p).wordmeanings.get(s).endAt+"");
								Meaning.setAttribute("AppearingWord", words.get(p).wordmeanings.get(s).appearingWord);
								CellValueSemantics.appendChild(Meaning);				
							}
						}
						}
						
						
						
						
						Element CellType = doc.createElement("CellType");
						CellType.setTextContent(cells[j][k].getCellType());
						CellEl.appendChild(CellType);
						
						
						for(int s = j-1;s>=0;s--)
						{
							//Current header not empty, and cell before is not header
							if(s>=0 && cells[j][k].isIs_header() && !cells[s][k].isIs_header()&&!cells[j][k].getCell_content().equals(""))
							{
								break;
							}
							
							if(s>=0&&cells[s][k]!=null && cells[s][k].isIs_header())
							{
								Element HeaderRef = doc.createElement("HeaderRef");
								HeaderRef.setTextContent(""+s+"."+k);
								CellEl.appendChild(HeaderRef);
								//break;
							}
							if(s>=0&&cells[s][k]!=null && cells[s][k].isIs_header())
							{
								Element HeaderCatRef = doc.createElement("HeadStubRef");
								HeaderCatRef.setTextContent(""+s+"."+0);
								CellEl.appendChild(HeaderCatRef);
								break;
							}
						}
						
						for(int s = k-1;s>=0;s--)
						{
							if(s>=0 && cells[j][s]!=null && cells[j][s].isIs_stub())
							{
								Element StubRef = doc.createElement("StubRef");
								StubRef.setTextContent(""+j+"."+s);
								CellEl.appendChild(StubRef);
								break;
							}
						}
						
						Element CellRoles = doc.createElement("CellRoles");
						CellEl.appendChild(CellRoles);
						boolean isDataCell = true;
						if(cells[j][k].isIs_header()){
							Element CellRole = doc.createElement("CellRole");
							CellRole.setTextContent("Header");
							CellRoles.appendChild(CellRole);
							isDataCell = false;
						}
						if(cells[j][k].isIs_stub()){
							Element CellRole = doc.createElement("CellRole");
							CellRole.setTextContent("Stub");
							CellRoles.appendChild(CellRole);
							isDataCell = false;
						}		
						
						boolean isSuperRow = false;
						for(int l = 0;l<5;l++)
						{
							if(cells[j][k]!=null&&j+l<cells.length&&cells[j+l][k]!=null&&cells[j+l][k].getSuperRowIndex()!=null&&cells[j+l][k].getSuperRowIndex().equals(cells[j][k].CellId))
							{
								isSuperRow = true;
								break;
							}
						}
						if(isSuperRow)
						{
							for(int l = 0; l<cells[j].length;l++)
							{
								cells[j][l].setIs_subheader(true);
							}
						}
						
						
						
						if(cells[j][k].isIs_subheader()){
							Element CellRole = doc.createElement("CellRole");
							CellRole.setTextContent("SuperRow");
							CellRoles.appendChild(CellRole);
						}
						//Previously was StubHeaderCell, but makes no sense when since Header is anyway included.
						if( k==0 && cells[j][k].isIs_header()){
							Element CellRole = doc.createElement("CellRole");
							CellRole.setTextContent("Stub");
							CellRoles.appendChild(CellRole);
							isDataCell = false;
						}
						if(isDataCell)
						{
							Element CellRole = doc.createElement("CellRole");
							CellRole.setTextContent("Data");
							CellRoles.appendChild(CellRole);
						}	
						
						Element CellRow = doc.createElement("CellRowNum");
						CellRow.setTextContent(j+"");//cells[j][k].getRow_number()+""
						CellEl.appendChild(CellRow);
						
						Element CellColumn = doc.createElement("CellColumnNum");
						CellColumn.setTextContent(k+"");//cells[j][k].getColumn_number()
						
						CellEl.appendChild(CellColumn);			
					}
				}
				}
			}						
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source;
			source = new DOMSource(doc);
			StreamResult result =  new StreamResult(new File("Annotation/"+a.getPmc()+".xml"));
			transformer.transform(source, result);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
