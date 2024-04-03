/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/

package io.github.rosemoe.sora.langs.monarch.theme

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import io.github.dingyi222666.monarch.loader.json.addLast

class TokenThemeAdapter : JsonAdapter<TokenTheme>() {
    override fun fromJson(reader: JsonReader): TokenTheme {
        reader.isLenient = true
        val tokenThemeRuleList = mutableListOf<TokenThemeRule>()
        val themeColorsMap = mutableMapOf<String, String>()
        var themeType = "light"

        // ignore name

        reader.beginObject()

        while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                "type" -> {
                    themeType = reader.nextString()
                }

                "tokenColors" -> {
                    readTokenColors(reader, tokenThemeRuleList)
                }

                "colors" -> {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        themeColorsMap[reader.nextName()] = reader.nextString()
                    }
                    reader.endObject()
                }
                else -> {
                    reader.skipValue()
                }
            }
        }

        reader.endObject()

        return TokenTheme.createFromRawTokenTheme(
            tokenThemeRuleList,
            emptyList(),
            ThemeDefaultColors(themeColorsMap),
            themeType,
        )

    }

    private fun readTokenColors(
        reader: JsonReader,
        tokenThemeRuleList: MutableList<TokenThemeRule>
    ) {
        reader.beginArray()

        while (reader.hasNext()) {
            reader.beginObject()
            val tokenList = mutableListOf<String>()
            var foreground: String? = null
            var background: String? = null
            var fontStyle: String? = null

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "scope" -> {
                        if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                tokenList.add(reader.nextString())
                            }
                            reader.endArray()
                        } else {
                            tokenList.add(reader.nextString())
                        }
                    }

                    "settings" -> {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "foreground" -> {
                                    foreground = reader.nextString()
                                }

                                "background" -> {
                                    background = reader.nextString()
                                }

                                "fontStyle" -> {
                                    fontStyle = reader.nextString()
                                }
                            }
                        }
                        reader.endObject()
                    }

                    else -> {
                        reader.skipValue()
                    }
                }

            }

            if (tokenList.isNotEmpty()) {
                for (token in tokenList) {
                    tokenThemeRuleList.add(
                        TokenThemeRule(
                            token,
                            foreground,
                            background,
                            fontStyle
                        )
                    )
                }
            }

            reader.endObject()
        }

        reader.endArray()
    }

    override fun toJson(p0: JsonWriter, p1: TokenTheme?) {
        TODO("Not yet implemented")
    }

}

internal val MoshiRoot: Moshi = Moshi.Builder()
    .addLast<TokenTheme>(TokenThemeAdapter())
    .build()

fun String.toTokenTheme(): TokenTheme {
    return MoshiRoot.adapter(TokenTheme::class.java).fromJson(this)!!
}

internal data class TokenThemeRule(
    override var token: String,
    override var foreground: String?,
    override var background: String?,
    override var fontStyle: String?
) : ITokenThemeRule