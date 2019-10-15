package com.openwatchproject.launcher.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.Activity.ClockSkinChooserActivity;
import com.openwatchproject.launcher.ClockSkinConstants;
import com.openwatchproject.launcher.Model.ClockInfo;
import com.openwatchproject.launcher.Model.WearWatchFace;
import com.openwatchproject.launcher.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ClockSkinFragment extends Fragment {
    private static final String TAG = "ClockSkinFragment";
    public static final int REQUEST_CODE_CHOOSE_CLOCK_SKIN = 1;

    private AnimationDrawable animationDrawable;

    private ConstraintLayout rootLayout;
    private ImageView imageView;
    private TextView textView;
    private LoadAnimation loadAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clock_skin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootLayout = view.findViewById(R.id.root_layout);
        rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent clockskinChooserIntent = new Intent(getContext(), ClockSkinChooserActivity.class);
                startActivityForResult(clockskinChooserIntent, REQUEST_CODE_CHOOSE_CLOCK_SKIN);
                return true;
            }
        });

        imageView = view.findViewById(R.id.imageView);
        textView = view.findViewById(R.id.loading);
        Point displaySize = getDisplaySize();
        imageView.getLayoutParams().height = displaySize.x;
        imageView.getLayoutParams().width = displaySize.y;
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.clock_skin_model));
        textView.setVisibility(View.GONE);

        getWatchFace();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_CLOCK_SKIN) {
            if (resultCode == Activity.RESULT_OK) {
                String clockskinPath = data.getStringExtra("clockskinPath");
                Log.d(TAG, "Selected clockskin: " + clockskinPath);
            } else {
                Log.d(TAG, "No clockskin selected");
            }
        }
    }

    @Override
    public void onDestroyView() {
        imageView = null;
        textView = null;
        super.onDestroyView();
    }

    private Point getDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    private int getMinDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return Math.min(size.x, size.y);
    }

    @DrawableRes
    public int getDrawableRes(String name) {
        try {
            return R.drawable.class.getField(name).getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        }
    }

    public class LoadAnimation extends AsyncTask<Void, Void, AnimationDrawable> {
        @Override
        protected AnimationDrawable doInBackground(Void... voids) {
            AnimationDrawable animation = new AnimationDrawable();
            animation.setOneShot(false);
            for (int i = 0; i < 25; i++) {
                BitmapDrawable f = (BitmapDrawable) ContextCompat.getDrawable(getContext(),
                        getDrawableRes("animation_1_" + i));
                animation.addFrame(f, 100);
            }
            return animation;
        }

        @Override
        protected void onPostExecute(AnimationDrawable animationDrawable) {
            ClockSkinFragment.this.animationDrawable = animationDrawable;
            imageView.setBackground(animationDrawable);
            animationDrawable.start();
            textView.setVisibility(View.GONE);
        }
    }

    public List<WearWatchFace> getWearWatchFaces() {
        Intent i = new Intent(WallpaperService.SERVICE_INTERFACE);
        i.addCategory("com.google.android.wearable.watchface.category.WATCH_FACE");

        List<ResolveInfo> availableWatchFaces = getContext().getPackageManager()
                .queryIntentServices(i, PackageManager.GET_META_DATA);
        List<WearWatchFace> wearWatchFaces = new ArrayList<>();

        for (ResolveInfo service : availableWatchFaces) {
            WearWatchFace wearWatchFace = new WearWatchFace(getContext(), service);
            wearWatchFaces.add(wearWatchFace);
        }

        return wearWatchFaces;
    }

    private Bitmap getClockskinPreview(File clockskinFile) {
        Bitmap preview = null;

        if (clockskinFile.isDirectory()) {
            preview = BitmapFactory.decodeFile(new File(clockskinFile, ClockSkinConstants.CLOCK_SKIN_PREVIEW).getAbsolutePath());
        } else {
            try (ZipFile clockskinZip = new ZipFile(clockskinFile)) {
                Enumeration<? extends ZipEntry> clockskinEntries = clockskinZip.entries();
                while (clockskinEntries.hasMoreElements()) {
                    ZipEntry clockskinEntry = clockskinEntries.nextElement();
                    if (clockskinEntry.getName().equals(ClockSkinConstants.CLOCK_SKIN_PREVIEW)) {
                        preview = BitmapFactory.decodeStream(clockskinZip.getInputStream(clockskinEntry));
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "getClockskinPreview: Unable to decode preview", e);
            }
        }

        return preview;
    }

    public void getWatchFace() {
        File clockskinFolder = new File(Environment.getExternalStorageDirectory(), "clockskin/");
        try (ZipFile zipFile = new ZipFile(new File(clockskinFolder, "AudiRS2.0.zip"))) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                switch (zipEntry.getName()) {
                    case ClockSkinConstants.CLOCK_SKIN_XML:
                        InputStream zipFileInputSteam = zipFile.getInputStream(zipEntry);
                        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(zipFileInputSteam);
                        doc.getDocumentElement().normalize();
                        Element root = doc.getDocumentElement();
                        Log.d(TAG, "Root element: " + root.getNodeName());
                        NodeList drawables = root.getChildNodes();
                        for (int i = 0; i < drawables.getLength(); i++) {
                            Node drawable = drawables.item(i);
                            if (drawable.getNodeType() == Node.ELEMENT_NODE) {
                                if (drawable instanceof Element
                                        && drawable.getNodeName().equals("drawable")) {
                                    ClockInfo clockInfo = parseDrawable((Element) drawable);
                                }
                            }
                        }
                        break;
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private ClockInfo parseDrawable(Element drawable) {
        ClockInfo clockInfo = new ClockInfo();

        Element element = (Element) drawable;
        NodeList elementNodes = element.getChildNodes();
        for (int j = 0; j < elementNodes.getLength(); j++) {
            Node n = elementNodes.item(j);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Log.d(TAG, "\t" + n.getNodeName() + ": " + n.getTextContent());
            }
        }

        return clockInfo;
    }

}
