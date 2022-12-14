/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.protocols.ss7.map.datacoding;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

import org.restcomm.protocols.ss7.map.api.datacoding.NationalLanguageIdentifier;

/**
 * <p>
 * The encoding/decoding of 7 bits characters in USSD strings is used doing GSMCharset.
 * </p>
 * <br/>
 * <p>
 * For further details look at GSM 03.38 Specs
 * </p>
 *
 * @author amit bhayani
 * @author sergey vetyutnev
 *
 */
public class GSMCharset extends Charset {

    public static final String GSM_CANONICAL_NAME = "GSM";

    protected static final float averageCharsPerByte = 8 / 7f;
    protected static final float maxCharsPerByte = 2f;

    protected static final float averageBytesPerChar = 2f;
    protected static final float maxBytesPerChar = 2f;

    protected static final int BUFFER_SIZE = 256;

    public static final byte ESCAPE = 0x1B;

    protected int[] mainTable;
    protected int[] extensionTable;

    public GSMCharset(String canonicalName, String[] aliases) {
        this(canonicalName, aliases, basicMap,basicExtentionMap);
    }

    public GSMCharset(String canonicalName, String[] aliases, int[] mainTable, int[] extentionTable) {
        super(canonicalName, aliases);

        this.mainTable = mainTable;
        this.extensionTable = extentionTable;
    }

    public GSMCharset(String canonicalName, String[] aliases,
            NationalLanguageIdentifier nationalLanguageLockingShiftIdentifier,
            NationalLanguageIdentifier nationalLanguageSingleShiftIdentifier) {
        super(canonicalName, aliases);

        if (nationalLanguageLockingShiftIdentifier == null) {
            this.mainTable = basicMap;
        } else {
            switch (nationalLanguageLockingShiftIdentifier) {
                case Spanish:
                    this.mainTable = basicMap;
                    break;
                case Portuguese:
                    this.mainTable = portugeseMap;
                    break;
                case Turkish:
                    this.mainTable = turkishMap;
                    break;
                case Urdu:
                    this.mainTable = urduMap;
                    break;
                case Hindi:
                    this.mainTable = hindiMap;
                    break;
                case Bengali:
                    this.mainTable = bengaliMap;
                    break;
                case Punjabi:
                    this.mainTable = punjabiMap;
                    break;
                case Gujarati:
                    this.mainTable = gujaratiMap;
                    break;
                case Oriya:
                    this.mainTable = oriyaMap;
                    break;
                case Tamil:
                    this.mainTable = tamilMap;
                    break;
                case Telugu:
                    this.mainTable = teluguMap;
                    break;
                case Kannada:
                    this.mainTable = kannadaMap;
                    break;
                case Malayalam:
                    this.mainTable = malayalamMap;
                    break;
                default:
                    this.mainTable = basicMap;
                    break;
            }
        }
        if (nationalLanguageSingleShiftIdentifier == null) {
            this.extensionTable = basicExtentionMap;
        } else
            switch (nationalLanguageSingleShiftIdentifier) {
                case Spanish:
                    this.extensionTable = spanishExtentionMap;
                    break;
                case Portuguese:
                    this.extensionTable = portugeseExtentionMap;
                    break;
                case Turkish:
                    this.extensionTable = turkishExtentionMap;
                    break;
                case Urdu:
                    this.extensionTable = urduExtentionMap;
                    break;
                case Hindi:
                    this.extensionTable = hindiExtentionMap;
                    break;
                case Bengali:
                    this.extensionTable = bengaliExtentionMap;
                    break;
                case Punjabi:
                    this.extensionTable = punjabiExtentionMap;
                    break;
                case Gujarati:
                    this.extensionTable = gujaratiExtentionMap;
                    break;
                case Oriya:
                    this.extensionTable = oriyaExtentionMap;
                    break;
                case Tamil:
                    this.extensionTable = tamilExtentionMap;
                    break;
                case Telugu:
                    this.extensionTable = teluguExtentionMap;
                    break;
                case Kannada:
                    this.extensionTable = kannadaExtentionMap;
                    break;
                case Malayalam:
                    this.extensionTable = malayalamExtentionMap;
                    break;
                default:
                    this.extensionTable = basicExtentionMap;
                    break;
            }
    }

    @Override
    public boolean contains(Charset cs) {
        return this.getClass().isInstance(cs);
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new GSMCharsetDecoder(this, averageCharsPerByte, maxCharsPerByte);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new GSMCharsetEncoder(this, averageBytesPerChar, maxBytesPerChar);
    }

    /**
     * Returns true if all characters in data String is included in main and extension encoding tables of the GSM7 charset
     *
     * @param data
     * @return
     */
    public boolean checkAllCharsCanBeEncoded(String data) {
        return checkAllCharsCanBeEncoded(data, this.mainTable, this.extensionTable);
    }

    /**
     * Returns true if all characters in data String is included in main and extension encoding tables of the GSM7 charset
     *
     * @param data
     * @return
     */
    public static boolean checkAllCharsCanBeEncoded(String data, int[] mainTable, int[] extentionTable) {
        if (data == null)
            return true;

        if (mainTable == null)
            return false;

        for (int i1 = 0; i1 < data.length(); i1++) {
            char c = data.charAt(i1);

            boolean found = false;
            for (int i = 0; i < mainTable.length; i++) {
                if (mainTable[i] == c) {
                    found = true;
                    break;
                }
            }
            if (!found && extentionTable != null) {
                for (int i = 0; i < extentionTable.length; i++) {
                    if (c != 0 && extentionTable[i] == c) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                return false;
        }

        return true;
    }

    /**
     * Returns a count in characters / septets of the data String after which the String will be GSM7 style encoded. For all
     * characters from the extension character table two bytes will be reserved. For all characters from the main character
     * table or which are not present in main or extension character tables one byte will be reserved.
     *
     * @param data
     * @return
     */
    public int checkEncodedDataLengthInChars(String data) {
        return checkEncodedDataLengthInChars(data, this.mainTable, this.extensionTable);
    }

    /**
     * Returns a count in characters / septets of the data String after which the String will be GSM7 style encoded. For all
     * characters from the extension character table two bytes will be reserved. For all characters from the main character
     * table or which are not present in main or extension character tables one byte will be reserved.
     *
     * @param data
     * @return
     */
    public static int checkEncodedDataLengthInChars(String data, int[] mainTable, int[] extentionTable) {
        if (data == null)
            return 0;

        if (mainTable == null)
            return 0;

        int cnt = 0;
        for (int i1 = 0; i1 < data.length(); i1++) {
            char c = data.charAt(i1);

            boolean found = false;
            for (int i = 0; i < mainTable.length; i++) {
                if (mainTable[i] == c) {
                    found = true;
                    cnt++;
                    break;
                }
            }
            if (!found && extentionTable != null) {
                for (int i = 0; i < extentionTable.length; i++) {
                    if (c != 0 && extentionTable[i] == c) {
                        found = true;
                        cnt += 2;
                        break;
                    }
                }
            }
            if (!found)
                cnt++;
        }

        return cnt;
    }

    /**
     * Calculates how many octets encapsulate the provides septets count.
     *
     * @param data
     * @return
     */
    public static int septetsToOctets(int septCnt) {
        int byteCnt = (septCnt + 1) * 7 / 8;
        return byteCnt;
    }

    /**
     * Calculates how many septets are encapsulated in the provides octets count.
     *
     * @param data
     * @return
     */
    public static int octetsToSeptets(int byteCnt) {
        int septCnt = (byteCnt * 8 - 1) / 7 + 1;
        return septCnt;
    }

    /**
     * Slicing of a data String into substrings that fits to characters / septets count in charCount parameter.
     *
     * @param data
     * @return
     */
    public String[] sliceString(String data, int charCount) {
        return sliceString(data, charCount, this.mainTable, this.extensionTable);
    }

    /**
     * Slicing of a data String into substrings that fits to characters / septets count in charCount parameter.
     *
     * @param data
     * @return
     */
    public static String[] sliceString(String data, int charCount, int[] mainTable, int[] extentionTable) {
        if (data == null)
            return null;

        if (mainTable == null)
            return null;

        ArrayList<String> res = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int chCnt = 0;
        for (int i1 = 0; i1 < data.length(); i1++) {
            char c = data.charAt(i1);

            boolean found = false;
            for (int i = 0; i < mainTable.length; i++) {
                if (mainTable[i] == c) {
                    found = true;
                    chCnt++;
                    if (chCnt > charCount) {
                        chCnt = 1;
                        res.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    sb.append(c);
                    break;
                }
            }
            if (!found && extentionTable != null) {
                for (int i = 0; i < extentionTable.length; i++) {
                    if (extentionTable[i] == c) {
                        found = true;
                        chCnt += 2;
                        if (chCnt > charCount) {
                            chCnt = 2;
                            res.add(sb.toString());
                            sb = new StringBuilder();
                        }
                        sb.append(c);
                        break;
                    }
                }
            }
            if (!found) {
                chCnt++;
                if (chCnt > charCount) {
                    chCnt = 1;
                    res.add(sb.toString());
                    sb = new StringBuilder();
                }
                sb.append(c);
            }
        }

        res.add(sb.toString());
        String[] arr = new String[res.size()];
        res.toArray(arr);
        return arr;
    }

    public static final  int[] basicMap = new int[] {(int)'@',(int)'??',(int)'$',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'\n',(int)'??',(int)'??',(int)'\r',(int)'??',(int)'??',(int)'??',(int)'_',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',0xffff,(int)'??',(int)'??',(int)'??',(int)'??',(int)' ',(int)'!',(int)'"',(int)'#',(int)'??',(int)'%',(int)'&',(int)'\'',(int)'(',(int)')',(int)'*',(int)'+',(int)',',(int)'-',(int)'.',(int)'/',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';',(int)'<',(int)'=',(int)'>',(int)'?',(int)'??',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??'};
    public static final int[] basicExtentionMap = { 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x000C, 0x0000, 0x0000, 0x000D, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'^', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'{', (int)'}', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetDefault = new GSMCharset(GSM_CANONICAL_NAME, new String[0], basicMap,basicExtentionMap);

    public static final int[] spanishExtentionMap = { 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x000C, 0x0000, 0x0000, 0x000D, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'^', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'{', (int)'}', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|', (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,(int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetSpanish = new GSMCharset(GSM_CANONICAL_NAME, new String[0], basicMap,spanishExtentionMap);

    public static final  int[] portugeseMap = new int[] {(int)'@',(int)'??',(int)'$',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'\n',(int)'??',(int)'??',(int)'\r',(int)'??',(int)'??',(int)'??',(int)'_',(int)'??',(int)'??',(int)'??',(int)'???',(int)'^',(int)'\\',(int)'???',(int)'??',(int)'|',0xffff,(int)'??',(int)'??',(int)'??',(int)'??',(int)' ',(int)'!',(int)'"',(int)'#',(int)'??',(int)'%',(int)'&',(int)'\'',(int)'(',(int)')',(int)'*',(int)'+',(int)',',(int)'-',(int)'.',(int)'/',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';',(int)'<',(int)'=',(int)'>',(int)'?',(int)'??',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'~',(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',(int)'??',(int)'??',(int)'`',(int)'??',(int)'??'};
    public static final int[] portugeseExtentionMap = { 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, (int)'??', 0x000C, (int)'??', (int)'??', 0x000D, (int)'??', (int)'??', 0x0000, 0x0000, (int)'??', (int)'??', (int)'^', (int)'??', (int)'??', (int)'??', (int)'??', (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'{', (int)'}', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|', (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', (int)'??', 0x0000, 0x0000, (int)'??'};
    public static Charset gsm7CharsetPortugese = new GSMCharset(GSM_CANONICAL_NAME, new String[0], portugeseMap,portugeseExtentionMap);

    public static final  int[] turkishMap = new int[] {(int)'@',(int)'??',(int)'$',(int)'??',(int)'???',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'\n',(int)'??',(int)'??',(int)'\r',(int)'??',(int)'??',(int)'??',(int)'_',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',0xffff,(int)'??',(int)'??',(int)'??',(int)'??',(int)' ',(int)'!',(int)'"',(int)'#',(int)'??',(int)'%',(int)'&',(int)'\'',(int)'(',(int)')',(int)'*',(int)'+',(int)',',(int)'-',(int)'.',(int)'/',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';',(int)'<',(int)'=',(int)'>',(int)'?',(int)'??',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??'};
    public static final int[] turkishExtentionMap = { 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x000C, 0x0000, 0x0000, 0x000D, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'^', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'{', (int)'}', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,(int)'??', 0x0000,(int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, (int)'???', 0x0000, (int)'??', 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'??', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetTurkish = new GSMCharset(GSM_CANONICAL_NAME, new String[0], turkishMap,turkishExtentionMap);

    public static final int[] urduMap = new int[] { (int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'\n',(int)'??',(int)'??',(int)'\r',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',0xffff,(int)'??',(int)'??',(int)'??',(int)'??',(int)' ',(int)'!',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)')',(int)'(',(int)'??',(int)'??',(int)',',(int)'??',(int)'.',(int)'??',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';',(int)'??',(int)'??',(int)'??',(int)'?',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',(int)'??',0x064D, 0x0650, 0x064F, 0x0657, 0x0657,(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',0x0655, 0x0651, 0x0653, 0x0656, 0x0670};
    public static final int[] urduExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', 0x0600, 0x0601, 0x0000, (int)'??', (int)'??', (int)'??', (int)'??', (int)'??', (int)'??',(int)'??', (int)'??', (int)'??', (int)'??', (int)'??', (int)'??', (int)'{', (int)'}', (int)'??', (int)'??', 0x0610, 0x0611, 0x0612, (int)'\\',0x0613, 0x0614, (int)'??', (int)'??', (int)'??', 0x0652, 0x0658, (int)'??', (int)'??', (int)'??', (int)'??', (int)'??', (int)'[', (int)'~',(int)']', (int)'??', (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetUrdu = new GSMCharset(GSM_CANONICAL_NAME, new String[0], urduMap, urduExtentionMap);

    public static final int[] hindiMap = new int[] { 0x0901, 0x0902, 0x0903, (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'\n',(int)'???',(int)'???',(int)'\r',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',0xffff,(int)'???',(int)'???',(int)'???',(int)'???',(int)' ',(int)'!',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)')',(int)'(',(int)'???',(int)'???',(int)',',(int)'???',(int)'.',(int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';',(int)'???',(int)'???',(int)'???',(int)'?',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', 0x093C,(int)'???', 0x093E, 0x093F, 0x0940, 0x0941, 0x0942, 0x0943, 0x0944, 0x0945, 0x0946, 0x0947, 0x0948, 0x0949, 0x094A, 0x094B, 0x094C, 0x094D, (int)'???',(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???'};
    public static final int[] hindiExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', 0x0951, 0x0952, (int)'{', (int)'}', 0x0953, 0x0954, 0x0958, 0x0959, 0x095A, (int)'\\',0x095B, 0x095C, 0x095D, 0x095E, 0x095F, (int)'???', (int)'???', 0x0962, 0x0963, (int)'???', (int)'???', 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetHindi = new GSMCharset(GSM_CANONICAL_NAME, new String[0], hindiMap, hindiExtentionMap);

    public static final int[] bengaliMap = new int[] { 0x0981, 0x0982, 0x0983, (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'\n',(int)'???',0x0000,(int)'\r',0x00,(int)'???',(int)'???', 0x0000, 0x0000,(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',0xffff,(int)'???',(int)'???',(int)'???',(int)'???',(int)' ',(int)'!',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)')',(int)'(',(int)'???',(int)'???',(int)',',(int)'???',(int)'.',(int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', 0x0000,(int)'???',(int)'???',(int)'?', 0x09AC,(int)'???',(int)'???',(int)'???',(int)'???', 0x0000,(int)'???', 0x0000, 0x0000, 0x0000,(int)'???',(int)'???',(int)'???',(int)'???', 0x09BC,(int)'???', 0x09BE, 0x09BF, 0x09C0, 0x09C1, 0x09C2, 0x09C3, 0x09C4, 0x0000, 0x0000, 0x09C7, 0x09C8, 0x0000, 0x0000, 0x09CB, 0x09CC, 0x09CD, (int)'???',(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',0x09D7, 0x09DC, 0x09DD, (int)'???', (int)'???'};
    public static final int[] bengaliExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', 0x09DF, 0x09E0, 0x09E1, 0x09E2, (int)'{', (int)'}', 0x09E3, (int)'???', (int)'???', (int)'???', (int)'???', (int)'\\',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetBengali = new GSMCharset(GSM_CANONICAL_NAME, new String[0], bengaliMap, bengaliExtentionMap);

    public static final int[] punjabiMap = new int[] { 0x0A01, 0x0A02, 0x0A03, (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', 0x0000,(int)'\n', 0x000,0x0000,(int)'\r',0x0000,(int)'???',(int)'???', 0x0000, 0x0000,(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',0xffff,(int)'???',(int)'???',(int)'???',(int)'???',(int)' ',(int)'!',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)')',(int)'(',(int)'???',(int)'???',(int)',',(int)'???',(int)'.',(int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', 0x0000,(int)'???',(int)'???',(int)'?',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', 0x0000,(int)'???', 0x0A33, 0x0000, (int)'???', 0x0A36, 0x0000,(int)'???',(int)'???', 0x0A3C, 0x0000, 0x0A3E, 0x0A3F, 0x0A40, 0x0A41, 0x0A42, 0x0000, 0x0000, 0x0000, 0x0000, 0x0A47, 0x0A48, 0x0000, 0x0000, 0x0A4B, 0x0A4C, 0x0A4D, 0x0A51,(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',0x0A70, 0x0A71, (int)'???', (int)'???', (int)'???'};
    public static final int[] punjabiExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', 0x0A59, 0x0A5A, (int)'{', (int)'}', 0x0A5B, (int)'???', 0x0A5E, 0x0A75, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetPunjabi = new GSMCharset(GSM_CANONICAL_NAME, new String[0], punjabiMap, punjabiExtentionMap);

    public static final int[] gujaratiMap = new int[] { 0x0A81, 0x0A82, 0x0A83, (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', (int)'???',(int)'\n', (int)'???',(int)'???',(int)'\r',0x0000,(int)'???',(int)'???', (int)'???', 0x0000,(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',0xffff,(int)'???',(int)'???',(int)'???',(int)'???',(int)' ',(int)'!',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)')',(int)'(',(int)'???',(int)'???',(int)',',(int)'???',(int)'.',(int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', 0x0000,(int)'???',(int)'???',(int)'?',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', 0x0000,(int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???',(int)'???',(int)'???', 0x0ABC, (int)'???', 0x0ABE, 0x0ABF, 0x0AC0, 0x0AC1, 0x0AC2, 0x0AC3, 0x0AC4, 0x0AC5, 0x0000, 0x0AC7, 0x0AC8, 0x0AC9, 0x0000, 0x0ACB, 0x0ACC, 0x0ACD, (int)'???',(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z',(int)'???', (int)'???', 0x0AE2, 0x0AE3, (int)'???'};
    public static final int[] gujaratiExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', 0x0000, 0x0000, (int)'{', (int)'}', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetGujarati = new GSMCharset(GSM_CANONICAL_NAME, new String[0], gujaratiMap, gujaratiExtentionMap);

    public static final int[] oriyaMap = new int[] { 0x0B01, 0x0B02, 0x0B03, (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', (int)'???',(int)'\n', (int)'???', 0x0000,(int)'\r',0x0000,(int)'???',(int)'???', 0x0000, 0x0000,(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',0xffff,(int)'???',(int)'???',(int)'???',(int)'???',(int)' ',(int)'!',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)')',(int)'(',(int)'???',(int)'???',(int)',',(int)'???',(int)'.',(int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', 0x0000,(int)'???',(int)'???',(int)'?',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', 0x0000,(int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???',(int)'???',(int)'???', 0x0B3C, (int)'???', 0x0B3E, 0x0B3F, 0x0B40, 0x0B41, 0x0B42, 0x0B43, 0x0B44, 0x0000, 0x0000, 0x0B47, 0x0B48, 0x0000, 0x0000, 0x0B4B, 0x0B4C, 0x0B4D, 0x0B56,(int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z', 0x0B57, (int)'???', (int)'???', 0x0B62, 0x0B63};
    public static final int[] oriyaExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', 0x0B5C, 0x0B5D, (int)'{', (int)'}', (int)'???', (int)'???', (int)'???', 0x0000, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetOriya = new GSMCharset(GSM_CANONICAL_NAME, new String[0], oriyaMap, oriyaExtentionMap);

    public static final int[] tamilMap = new int[] { 0x00, 0x0B82, 0x0B83, (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', 0x0000,(int)'\n', 0x0000, 0x0000,(int)'\r', (int)'???',(int)'???',(int)'???', 0x0000, (int)'???', (int)'???',(int)'???',(int)'???', 0x0000, 0x0000, 0x0000,(int)'???',(int)'???',0xffff, 0x0000,(int)'???', 0x0000, (int)'???', (int)' ',(int)'!',(int)'???', 0x0000, 0x0000, 0x0000, (int)'???',(int)'???',(int)')',(int)'(', 0x0000, 0x0000,(int)',', 0x0000, (int)'.', (int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', (int)'???', (int)'???', 0x0000,(int)'?', 0x0000, 0x0000,(int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???',(int)'???', 0x0000, 0x0000, 0x0BBE, 0x0BBF, 0x0BC0, 0x0BC1, 0x0BC2, 0x0000, 0x0000, 0x0000, 0x0BC6, 0x0BC7, 0x0BC8, 0x0000, 0x0BCA, 0x0BCB, 0x0BCC, 0x0BCD, (int)'???', (int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z', 0x0BD7, (int)'???', (int)'???', (int)'???', (int)'???'};
    public static final int[] tamilExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'{', (int)'}', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetTamil = new GSMCharset(GSM_CANONICAL_NAME, new String[0], tamilMap, tamilExtentionMap);

    public static final int[] teluguMap = new int[] { 0x0C01, 0x0C02, 0x0C03, (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', (int)'???',(int)'\n', (int)'???', 0x0000,(int)'\r', (int)'???',(int)'???',(int)'???', 0x0000, (int)'???', (int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???',(int)'???',(int)'???',0xffff, (int)'???',(int)'???', (int)'???', (int)'???', (int)' ',(int)'!',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???',(int)')',(int)'(', (int)'???', (int)'???',(int)',', (int)'???', (int)'.', (int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', 0x0000, (int)'???', (int)'???',(int)'?', (int)'???', (int)'???',(int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???',(int)'???',(int)'???', 0x0000, (int)'???', 0x0C3E, 0x0C3F, 0x0C40, 0x0C41, 0x0C42, 0x0C43, 0x0C44, 0x0000, 0x0C46, 0x0C47, 0x0C48, 0x0000, 0x0C4A, 0x0C4B, 0x0C4C, 0x0C4D, 0x0C55, (int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z', 0x0C56, (int)'???', (int)'???', 0x0C62, 0x0C63};
    public static final int[] teluguExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', 0x0000, 0x0000, 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'{', (int)'}', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'\\', (int)'???', (int)'???', (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetTelugu = new GSMCharset(GSM_CANONICAL_NAME, new String[0], teluguMap, teluguExtentionMap);

    public static final int[] kannadaMap = new int[] { 0x00, (int)'???', (int)'???', (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', (int)'???', (int)'\n', (int)'???', 0x0000,(int)'\r', (int)'???',(int)'???',(int)'???', 0x0000, (int)'???', (int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', 0xffff, (int)'???',(int)'???', (int)'???', (int)'???', (int)' ',(int)'!',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???',(int)')',(int)'(', (int)'???', (int)'???',(int)',', (int)'???', (int)'.', (int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', 0x0000, (int)'???', (int)'???',(int)'?', (int)'???', (int)'???',(int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???'};
    public static final int[] kannadaExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'{', (int)'}', (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, (int)'\\', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'|',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetKannada = new GSMCharset(GSM_CANONICAL_NAME, new String[0], kannadaMap, kannadaExtentionMap);

    public static final int[] malayalamMap = new int[] { 0x00, (int)'???', (int)'???', (int)'???',(int)'???',(int)'???',(int)'???',(int)'???',(int)'???', (int)'???', (int)'\n', (int)'???', 0x0000,(int)'\r', (int)'???',(int)'???',(int)'???', 0x0000, (int)'???', (int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', 0xffff, (int)'???',(int)'???', (int)'???', (int)'???', (int)' ',(int)'!',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???',(int)')',(int)'(', (int)'???', (int)'???',(int)',', (int)'???', (int)'.', (int)'???',(int)'0',(int)'1',(int)'2',(int)'3',(int)'4',(int)'5',(int)'6',(int)'7',(int)'8',(int)'9',(int)':',(int)';', 0x0000, (int)'???', (int)'???',(int)'?', (int)'???', (int)'???',(int)'???',(int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???',(int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'a',(int)'b',(int)'c',(int)'d',(int)'e',(int)'f',(int)'g',(int)'h',(int)'i',(int)'j',(int)'k',(int)'l',(int)'m',(int)'n',(int)'o',(int)'p',(int)'q',(int)'r',(int)'s',(int)'t',(int)'u',(int)'v',(int)'w',(int)'x',(int)'y',(int)'z', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???'};
    public static final int[] malayalamExtentionMap = { (int)'@', (int)'??', (int)'$', (int)'??', (int)'??', (int)'"',(int)'??', (int)'%', (int)'&', (int)'\'', 0x000C, (int)'*', (int)'+', 0x000D, (int)'-', (int)'/', (int)'<', (int)'=', (int)'>', (int)'??',(int)'^', (int)'??', (int)'_', (int)'#', (int)'*', (int)'???', (int)'???', 0x0000, (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???',(int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'{', (int)'}', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', (int)'\\', (int)'???', (int)'???', (int)'???', (int)'???', (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'[', (int)'~',(int)']', 0x0000, (int)'-',(int)'A',(int)'B',(int)'C',(int)'D',(int)'E',(int)'F',(int)'G',(int)'H',(int)'I',(int)'J',(int)'K',(int)'L',(int)'M',(int)'N',(int)'O',(int)'P',(int)'Q',(int)'R',(int)'S',(int)'T',(int)'U',(int)'V',(int)'W',(int)'X',(int)'Y',(int)'Z', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, (int)'???', 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
    public static Charset gsm7CharsetMalayalam = new GSMCharset(GSM_CANONICAL_NAME, new String[0], malayalamMap, malayalamExtentionMap);
}