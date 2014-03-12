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

public class Parity{
    
    private Parity(){
    
    
    }
    
    public static String calculateParity(String hex, int parityBitLength){
    
        long parity = 0
        hex = hex.replaceAll("0x", "")
        hex = hex.replaceAll(" ", "")
        hex = hex.replaceAll("\r\n", "")
        hex = hex.replaceAll("\n", "")
        
        if((hex.length() * 4) % parityBitLength != 0) {
            throw new IllegalArgumentException("Invalid word length found!")
        }    
        
        java.util.List<Long> pieces = new ArrayList<Long>()
        
        StringBuilder sb = new StringBuilder(hex)
        
        while(sb.length() >= (parityBitLength / 4)){
            pieces.add(Long.decode("0x" + sb.substring(0, (int) (parityBitLength / 4))))
            sb.delete(0, (int)(parityBitLength / 4))
        }   
        
        for(long piece : pieces){
            parity = parity ^ piece
        }
        
        return "0x" + Long.toHexString(parity).toUpperCase()
    }
 }