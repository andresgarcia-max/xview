package com.android.tv.twopanelsettings.slices;

import android.graphics.drawable.Icon;
import android.net.Uri;
import android.util.Pair;
import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.SliceContent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public final class SlicePreferencesUtil {
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:15:0x013f  */
    /* JADX WARN: Type inference failed for: r2v28, types: [com.android.tv.twopanelsettings.slices.SliceCheckboxPreference] */
    /* JADX WARN: Type inference failed for: r2v29, types: [androidx.preference.Preference, com.android.tv.twopanelsettings.slices.RadioPreference, com.android.tv.twopanelsettings.slices.SliceRadioPreference] */
    /* JADX WARN: Type inference failed for: r2v30, types: [com.android.tv.twopanelsettings.slices.SliceSeekbarPreference] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static androidx.preference.Preference getPreference(androidx.slice.SliceItem r11, android.view.ContextThemeWrapper r12, java.lang.String r13, boolean r14) {
        /*
            Method dump skipped, instructions count: 691
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.tv.twopanelsettings.slices.SlicePreferencesUtil.getPreference(androidx.slice.SliceItem, android.view.ContextThemeWrapper, java.lang.String, boolean):androidx.preference.Preference");
    }

    static class Data {
        SliceItem mFollowupIntentItem;
        SliceItem mIntentItem;
        SliceItem mStartItem;
        SliceItem mSubtitleItem;
        SliceItem mSummaryItem;
        SliceItem mTargetSliceItem;
        SliceItem mTitleItem;
        List<SliceItem> mEndItems = new ArrayList();
        List<SliceItem> mInfoItems = new ArrayList();

        Data() {
        }
    }

    static Data extract(SliceItem sliceItem) {
        Data data = new Data();
        List<SliceItem> findAll = SliceQuery.findAll(sliceItem, (String) null, "title", (String) null);
        if (findAll.size() > 0) {
            String format = findAll.get(0).getFormat();
            if (("action".equals(format) && SliceQuery.find(findAll.get(0), "image") != null) || "slice".equals(format) || "long".equals(format) || "image".equals(format)) {
                data.mStartItem = findAll.get(0);
            }
        }
        List<SliceItem> items = sliceItem.getSlice().getItems();
        for (int i = 0; i < items.size(); i++) {
            SliceItem sliceItem2 = items.get(i);
            String subType = sliceItem2.getSubType();
            if (subType != null) {
                switch (subType) {
                    case "SUBTYPE_FOLLOWUP_INTENT":
                        data.mFollowupIntentItem = sliceItem2;
                        break;
                    case "SUBTYPE_INTENT":
                        data.mIntentItem = sliceItem2;
                        break;
                    case "TAG_TARGET_URI":
                        data.mTargetSliceItem = sliceItem2;
                        break;
                    case "SUBTYPE_INFO_PREFERENCE":
                        data.mInfoItems.add(sliceItem2);
                        break;
                }
            } else if ("text".equals(sliceItem2.getFormat()) && sliceItem2.getSubType() == null) {
                SliceItem sliceItem3 = data.mTitleItem;
                if ((sliceItem3 == null || !sliceItem3.hasHint("title")) && sliceItem2.hasHint("title") && !sliceItem2.hasHint("summary")) {
                    data.mTitleItem = sliceItem2;
                } else if (data.mSubtitleItem == null && !sliceItem2.hasHint("summary")) {
                    data.mSubtitleItem = sliceItem2;
                } else if (data.mSummaryItem == null && sliceItem2.hasHint("summary")) {
                    data.mSummaryItem = sliceItem2;
                }
            } else {
                data.mEndItems.add(sliceItem2);
            }
        }
        data.mEndItems.remove(data.mStartItem);
        return data;
    }

    private static List<Pair<CharSequence, CharSequence>> getInfoList(List<SliceItem> list) {
        ArrayList arrayList = new ArrayList();
        Iterator<SliceItem> it = list.iterator();
        while (it.hasNext()) {
            Slice slice = it.next().getSlice();
            if (slice != null) {
                CharSequence charSequence = null;
                CharSequence charSequence2 = null;
                for (SliceItem sliceItem : slice.getItems()) {
                    if (sliceItem.getHints().contains("title")) {
                        charSequence = sliceItem.getText();
                    } else if (sliceItem.getHints().contains("summary")) {
                        charSequence2 = sliceItem.getText();
                    }
                }
                arrayList.add(new Pair(charSequence, charSequence2));
            }
        }
        return arrayList;
    }

    private static CharSequence getKey(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "text", "TAG_KEY");
        if (findSubtype != null) {
            return findSubtype.getText();
        }
        return null;
    }

    private static CharSequence getRadioGroup(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "text", "TAG_RADIO_GROUP");
        if (findSubtype != null) {
            return findSubtype.getText();
        }
        return null;
    }

    static SliceItem getScreenTitleItem(List<SliceContent> list) {
        Iterator<SliceContent> it = list.iterator();
        while (it.hasNext()) {
            SliceItem sliceItem = it.next().getSliceItem();
            if (sliceItem.getSubType() != null && sliceItem.getSubType().equals("TYPE_PREFERENCE_SCREEN_TITLE")) {
                return sliceItem;
            }
        }
        return null;
    }

    static SliceItem getRedirectSlice(List<SliceContent> list) {
        Iterator<SliceContent> it = list.iterator();
        while (it.hasNext()) {
            SliceItem sliceItem = it.next().getSliceItem();
            if (sliceItem.getSubType() != null && sliceItem.getSubType().equals("TYPE_REDIRECTED_SLICE_URI")) {
                return sliceItem;
            }
        }
        return null;
    }

    static SliceItem getFocusedPreferenceItem(List<SliceContent> list) {
        Iterator<SliceContent> it = list.iterator();
        while (it.hasNext()) {
            SliceItem sliceItem = it.next().getSliceItem();
            if (sliceItem.getSubType() != null && sliceItem.getSubType().equals("TYPE_FOCUSED_PREFERENCE")) {
                return sliceItem;
            }
        }
        return null;
    }

    static SliceItem getEmbeddedItem(List<SliceContent> list) {
        Iterator<SliceContent> it = list.iterator();
        while (it.hasNext()) {
            SliceItem sliceItem = it.next().getSliceItem();
            if (sliceItem.getSubType() != null && sliceItem.getSubType().equals("TYPE_PREFERENCE_EMBEDDED")) {
                return sliceItem;
            }
        }
        return null;
    }

    private static boolean isIconNeedsToBeProcessed(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("SUBTYPE_ICON_NEED_TO_BE_PROCESSED")) {
                return sliceItem2.getInt() == 1;
            }
        }
        return false;
    }

    private static int getButtonStyle(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("SUBTYPE_BUTTON_STYLE")) {
                return sliceItem2.getInt();
            }
        }
        return -1;
    }

    private static int getSeekbarMin(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("SUBTYPE_SEEKBAR_MIN")) {
                return sliceItem2.getInt();
            }
        }
        return -1;
    }

    private static int getSeekbarMax(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("SUBTYPE_SEEKBAR_MAX")) {
                return sliceItem2.getInt();
            }
        }
        return -1;
    }

    private static int getSeekbarValue(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("SUBTYPE_SEEKBAR_VALUE")) {
                return sliceItem2.getInt();
            }
        }
        return -1;
    }

    private static boolean enabled(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("SUBTYPE_IS_ENABLED")) {
                return sliceItem2.getInt() == 1;
            }
        }
        return true;
    }

    private static boolean selectable(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("SUBTYPE_IS_SELECTABLE")) {
                return sliceItem2.getInt() == 1;
            }
        }
        return true;
    }

    private static boolean addInfoStatus(SliceItem sliceItem) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("extra_preference_add_info_status")) {
                return sliceItem2.getInt() == 1;
            }
        }
        return true;
    }

    static CharSequence getText(SliceItem sliceItem) {
        if (sliceItem == null) {
            return null;
        }
        return sliceItem.getText();
    }

    static Icon getIcon(SliceItem sliceItem) {
        if (sliceItem == null || sliceItem.getSlice() == null || sliceItem.getSlice().getItems() == null || sliceItem.getSlice().getItems().size() <= 0) {
            return null;
        }
        SliceItem sliceItem2 = sliceItem.getSlice().getItems().get(0);
        if ("image".equals(sliceItem2.getFormat())) {
            return sliceItem2.getIcon().toIcon();
        }
        return null;
    }

    static Uri getStatusPath(String str) {
        return Uri.parse(str).buildUpon().path("/status").build();
    }

    static int getPageId(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "int", "extra_page_id");
        if (findSubtype != null) {
            return findSubtype.getInt();
        }
        return 0;
    }

    private static int getActionId(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "int", "extra_action_id");
        if (findSubtype != null) {
            return findSubtype.getInt();
        }
        return 0;
    }

    private static CharSequence getInfoText(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "text", "extra_preference_info_text");
        if (findSubtype != null) {
            return findSubtype.getText();
        }
        return null;
    }

    private static CharSequence getInfoSummary(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "text", "extra_preference_info_summary");
        if (findSubtype != null) {
            return findSubtype.getText();
        }
        return null;
    }

    private static IconCompat getInfoImage(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "image", "extra_preference_info_image");
        if (findSubtype != null) {
            return findSubtype.getIcon();
        }
        return null;
    }

    private static IconCompat getInfoTitleIcon(SliceItem sliceItem) {
        SliceItem findSubtype = SliceQuery.findSubtype(sliceItem, "image", "extra_preference_info_title_icon");
        if (findSubtype != null) {
            return findSubtype.getIcon();
        }
        return null;
    }

    private static String getInfoContentDescription(SliceItem sliceItem, String str) {
        for (SliceItem sliceItem2 : sliceItem.getSlice().getItems()) {
            if (sliceItem2.getSubType() != null && sliceItem2.getSubType().equals("content_description")) {
                return sliceItem2.getText().toString();
            }
        }
        return str;
    }
}
