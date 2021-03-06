/*******************************************************************************
 * Copyright 2012 Patrick O'Leary
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.pjaol.ESB.formatters;

import java.text.Normalizer;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

public class StringSimplifier {

    public static final char DEFAULT_REPLACE_CHAR = '-';
    public static final String DEFAULT_REPLACE = String.valueOf(DEFAULT_REPLACE_CHAR);
    private static final ImmutableMap<String, String> NONDIACRITICS = ImmutableMap.<String, String>builder()
            //remove crap strings with no sematics
            
            .put("°", DEFAULT_REPLACE) //remove ?? is diacritic?
                    //replace non-diacritics as their equivalent chars
            .put("\u0141", "l") // BiaLystock
            .put("\u0142", "l") // Bialystock
            .put("ß", "ss")
            .put("æ", "ae")
            .put("ø", "o")
            .put("©", "c")
            .put("\u00D0", "d") // all Ð ð from http://de.wikipedia.org/wiki/%C3%90
            .put("\u00F0", "d")
            .put("\u0110", "d")
            .put("\u0111", "d")
            .put("\u0189", "d")
            .put("\u0256", "d")
            .put("\u00DE", "th") // thorn Þ 
            .put("\u00FE", "th") // thorn þ
            .build();


    public static String simplifiedString(String orig) {
            String str = orig;
            if (str == null) {
                    return null;
            }
            str = stripDiacritics(str);
            str = stripNonDiacritics(str);
            if (str.length() == 0) {
                    // ugly special case to work around non-existing empty strings in oracle. store original crapstring as simplified..
                    //  would return empty string if oracle could store it.
                    return orig;
            }
            return str;
    }

    private static String stripNonDiacritics(String orig) {
            StringBuffer ret = new StringBuffer();
            String lastchar = null;
            for (int i = 0; i < orig.length(); i++) {
                    String source = orig.substring(i, i + 1);
                    String replace = NONDIACRITICS.get(source);
                    String toReplace = replace == null ? String.valueOf(source) : replace;
                    if (DEFAULT_REPLACE.equals(lastchar) && DEFAULT_REPLACE.equals(toReplace)) {
                            toReplace = "";
                    } else {
                            lastchar = toReplace;
                    }
                    ret.append(toReplace);
            }
            if (ret.length() > 0 && DEFAULT_REPLACE_CHAR == ret.charAt(ret.length() - 1)) {
                    ret.deleteCharAt(ret.length() - 1);
            }
            return ret.toString();
    }

    /*
    special regexp char ranges relevant for simplification -> see http://docstore.mik.ua/orelly/perl/prog3/ch05_04.htm      
    InCombiningDiacriticalMarks: special marks that are part of "normal" ä, ö, î etc..
            IsSk: Symbol, Modifier see http://www.fileformat.info/info/unicode/category/Sk/list.htm
            IsLm: Letter, Modifier see http://www.fileformat.info/info/unicode/category/Lm/list.htm
     */
    public static final Pattern DIACRITICS_AND_FRIENDS 
            = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");


    private static String stripDiacritics(String str) {
            str = Normalizer.normalize(str, Normalizer.Form.NFD);
            str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
            return str;
    }
}
