/*
    General File Utility
    Copyright (C) 2012-2014, Gary Paduana, gary.paduana@gmail.com
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package gp.gfu.domain


public class SeriesRenameableCollection extends AbstractRenameableCollection{
	
	private String resolution = ""
	private String seriesName = ""
	private String outputSpec = ""
	private String episodeName = ""
	private String extension = ""
	private int season = 0
	private int episode = 0
	
	public SeriesRenameableCollection(String topDir, String regex, String seriesName, String outputSpec){
		super(topDir, regex)
		this.seriesName = seriesName
		this.outputSpec = outputSpec
	}
		
	String applyChange(String name, File file){
		name = stripExcess(name)
		season = findSeason(name)
		episode = findEpisode(name)
		
		resolution = findResolution(name)
		name = name.replace(resolution, "")
		
		extension = name.substring(name.lastIndexOf(".") + 1, name.length())
		name = name.replace(extension, "")
		name = name.substring(0, name.length() - 1)
		
		episodeName = findEpisodeName(name, seriesName)
		
		String spec = outputSpec
		spec = spec.replace("%sn", seriesName)
		spec = spec.replace("%se", "s" + season.toString().padLeft(2, "0") + "e" + episode.toString().padLeft(2, "0"))
		spec = spec.replace("%en", episodeName.size() > 0 ? episodeName : "Unnamed")
		spec = spec.replace("%res", resolution.size() > 0 ? resolution : "Unknown resolution")
		spec = spec.replace("%ext", extension)
		
		return spec
	}
		
	private String findEpisodeName(String name, String seriesName){
		java.util.List<String> seriesNameWords = new ArrayList<String>()
		seriesNameWords.addAll(Arrays.asList(seriesName.split(" ")))
		
		for(String s : seriesNameWords){
            if(!s.equals(" ")){
                name = name.toLowerCase().replaceFirst(s.toLowerCase(), "")
            }
        }
		
		java.util.List<String> filteredWords = new ArrayList<String>()
		filteredWords.addAll(Arrays.asList(name.split(" ")))
		
		for(Iterator it = filteredWords.iterator(); it.hasNext();){
			if(!(it.next() ==~ /[a-zA-Z,'!@$.:]+/)){
				it.remove()
			}
		}
		
		StringBuilder sb = new StringBuilder()
		for(String s : filteredWords){
			s = s.replaceFirst(s.substring(0,1), s.substring(0,1).toUpperCase())
			sb.append(s + " ")
		}
		if(sb.size() > 0){
			sb.deleteCharAt(sb.size() - 1)
		}
		
		return sb.toString()
	}
	
	private String stripExcess(String name){
		java.util.List<String> excessList = ["hdtv", "x264", "divx", "xvid",											 
											 /\[/, /\]/, "ac3", "mp3", "kbps"]
		
		for(String s : excessList){
			name = name.toLowerCase().replaceAll(s, " ")
		}
		
		return name
	}
	
	private String findResolution(String name){
		def matcher = name =~ /.+?(\d{3,}p).+/
		if(matcher){
			return matcher[0][1]
		}
		else return ""
	}
	
	private int findEpisode(String name){
		java.util.List<String> seasonExpressions = new ArrayList<String>()
		seasonExpressions.add(/.+?\d+x(\d+).+?/)
		seasonExpressions.add(/.+[e|E|ep|Ep|EP|eP](\d+).+?/)
		seasonExpressions.add(/.+?\d+[^0-9]?(\d+).+?/)
	
		for(String exp : seasonExpressions){
			def matcher = name =~ exp
			if(matcher){
				return Integer.valueOf(matcher[0][1])
			}
		}
		
		return 0
	}
	
	private int findSeason(String name){
		
		java.util.List<String> seasonExpressions = new ArrayList<String>()
		seasonExpressions.add(/.+?(\d+)x\d+.+/)
        seasonExpressions.add(/.+[s|S](\d+).+/)
        seasonExpressions.add(/.+?(\d+)[^0-9]?\d+.+/)
		
		for(String exp : seasonExpressions){
			def matcher = name =~ exp
			if(matcher){
				return Integer.valueOf(matcher[0][1])
			}
		}
		
		return 0
	}
 }