/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 qwq233@qwq2333.top
 * https://github.com/cinit/QAuxiliary
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */
package xyz.nextalone.hook

import io.github.qauxv.base.annotation.FunctionHookEntry
import io.github.qauxv.base.annotation.UiItemAgentEntry
import io.github.qauxv.dsl.FunctionEntryRouter
import io.github.qauxv.util.QQVersion
import io.github.qauxv.util.requireMinQQVersion
import xyz.nextalone.base.MultiItemDelayableHook
import xyz.nextalone.util.clazz
import xyz.nextalone.util.get
import xyz.nextalone.util.hookBefore
import xyz.nextalone.util.method
import xyz.nextalone.util.throwOrTrue

@FunctionHookEntry
@UiItemAgentEntry
object SimplifyEmoPanel : MultiItemDelayableHook("na_simplify_emo_panel") {
    override val preferenceTitle = "精简表情菜单"

    override val uiItemLocation = FunctionEntryRouter.Locations.Simplify.CHAT_EMOTICON

    private val allItemsDict = mapOf(13 to "加号菜单", 7 to "默认表情", 4 to "收藏表情", 12 to "热门表情", 15 to "厘米秀", 11 to "DIY表情", 9 to "魔法表情", 17 to "超级QQ秀", -1 to "表情包")
    override val allItems: Set<String> = allItemsDict.values.toSet()
    override val enableCustom = false

    override fun initOnce() = throwOrTrue {
        ("com.tencent.mobileqq.emoticonview.BasePanelView".clazz
            ?: "com.tencent.mobileqq.emoticonview.EmoticonPanelController".clazz
            )!!.method("initTabView")?.hookBefore(
                this
            ) {
                val mutableList: MutableList<*> =
                    if ("com.tencent.mobileqq.emoticonview.BasePanelModel".clazz != null) {
                        it.thisObject.get("mPanelController").get("mBasePanelModel").get("panelDataList") as MutableList<*>
                    } else {
                        it.thisObject.get("panelDataList") as MutableList<*>
                    }
                val list = mutableList.listIterator()
                while (list.hasNext()) {
                    val item = list.next()
                    if (item != null) {
                        val i = item.javaClass.getDeclaredField("type").get(item) as Int
                        if (allItemsDict[i] in activeItems || i !in allItemsDict.keys && "表情包" in activeItems) {
                            list.remove()
                        }
                    }
                }
                // fixme unable to locate the slide method
//                "Lcom/tencent/mobileqq/emoticonview/EmoticonTabAdapter;->getView(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;".method.hookAfter(
//                    this@SimplifyEmoPanel
//                ) { it2 ->
//                    val view: View = it2.result as View
//                    val layoutParams: ViewGroup.LayoutParams = view.layoutParams
//                    layoutParams.width = hostInfo.application.resources.displayMetrics.widthPixels / mutableList.size
//                    view.layoutParams = layoutParams
//                    it2.result = view
//                }
            }
    }

    override val isAvailable: Boolean get() = requireMinQQVersion(QQVersion.QQ_8_5_5)
}
