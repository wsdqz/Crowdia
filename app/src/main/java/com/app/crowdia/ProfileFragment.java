package com.app.crowdia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import classes.Auth;
import classes.User;

import static android.content.Context.MODE_PRIVATE;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private TextView logoutButton;
    private SharedPreferences preferences;
    private TextView profileName, profileEmail, balanceText;
    private ImageView profileImage;
    private CardView profileImageContainer;
    private Button addFundsButton;
    private View adminPanelSection, manageUsersButton, manageProjectsButton;
    
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_GALLERY = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        preferences = getActivity().getSharedPreferences("app", MODE_PRIVATE);
        
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        balanceText = view.findViewById(R.id.balanceText);
        profileImage = view.findViewById(R.id.profileImage);
        profileImageContainer = view.findViewById(R.id.profileImageContainer);
        addFundsButton = view.findViewById(R.id.addFundsButton);
        
        // элементы админ-панели
        adminPanelSection = view.findViewById(R.id.adminPanelSection);
        manageUsersButton = view.findViewById(R.id.manageUsersButton);
        manageProjectsButton = view.findViewById(R.id.manageProjectsButton);
        
        logoutButton = view.findViewById(R.id.logoutButton);
        View myProjectsButton = view.findViewById(R.id.myProjectsButton);
        View transactionsButton = view.findViewById(R.id.transactionsButton);
        View settingsButton = view.findViewById(R.id.settingsButton);
        View accountButton = view.findViewById(R.id.accountButton);
        
        // обработчик нажатия на аватар
        profileImageContainer.setOnClickListener(v -> {
            showImagePickerDialog();
        });
        
        logoutButton.setOnClickListener(v -> logout());
        
        myProjectsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserProjectsActivity.class);
            startActivity(intent);
        });
        
        transactionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DonationHistoryActivity.class);
            startActivity(intent);
        });
        
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
        
        accountButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AccountActivity.class);
            startActivity(intent);
        });
        
        addFundsButton.setOnClickListener(v -> {
            showAddFundsDialog();
        });
        
        manageUsersButton.setOnClickListener(v -> {
            showManageUsersDialog();
        });
        
        manageProjectsButton.setOnClickListener(v -> {
            showManageProjectsDialog();
        });
        
        updateUserInfo();
        
        return view;
    }
    
    private void showImagePickerDialog() {
        String[] options = new String[]{getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_profile_image);
        
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // камера
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CAMERA);
            } else if (which == 1) { // галерея
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });
        
        AlertDialog alertDialog = builder.show();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) { // камера
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                saveAvatarToFB(bitmap);
                profileImage.setImageBitmap(bitmap);
            } else if (requestCode == REQUEST_GALLERY) { // галерея
                try {
                    InputStream is = getActivity().getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    saveAvatarToFB(bitmap);
                    profileImage.setImageBitmap(bitmap);
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), R.string.image_loading_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    public void updateUserInfo() {
        if (Auth.signedInUser != null) {
            User user = Auth.signedInUser;
            
            // имя и email
            profileName.setText(user.getUsername());
            profileEmail.setText(user.getEmail());
            
            // баланс
            balanceText.setText(String.format("%.0f ₸", user.getBalance()));
            
            // аватар (если есть)
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(user.getAvatar(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profileImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    // дефолтное изображение в случае ошибки
                }
            }
            
            // показываем/скрываем админ панель
            if (user.isAdmin()) {
                adminPanelSection.setVisibility(View.VISIBLE);
            } else {
                adminPanelSection.setVisibility(View.GONE);
            }
        }
    }
    
    private void saveAvatarToFB(Bitmap bitmap) {
        // сжатие изображения
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 400);
        
        // конверт в Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        
        Auth.signedInUser.setAvatar(encoded);
        
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Crowdia").child("Users");
        usersRef.child(Auth.signedInUser.getKey()).setValue(Auth.signedInUser);
        
        Toast.makeText(getActivity(), R.string.avatar_updated, Toast.LENGTH_SHORT).show();
    }
    
    // изменение размера изображения
    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    
    private void logout() {
        // удаляем данные пользователя из SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("userkey");
        editor.apply();
        
        // очищаем информацию о текущем пользователе
        Auth.signedInUser = null;
        
        Toast.makeText(getActivity(), R.string.logout_message, Toast.LENGTH_SHORT).show();
        
        // переходим на экран входа
        Intent intent = new Intent(getActivity(), LoginActivity.class);

        // очищаем все предыдущие активити
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        getActivity().finish();
    }

    private void showAddFundsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_funds_title);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_funds, null);
        builder.setView(dialogView);
        
        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        
        builder.setPositiveButton(R.string.add_funds_button, null); //  null чтобы предотвратить автоматическое закрытие
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (!TextUtils.isEmpty(amountStr)) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(getActivity(), R.string.amount_greater_than_zero, Toast.LENGTH_SHORT).show();
                    } else {
                        addFundsToBalance(amount);
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), R.string.enter_valid_amount, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.enter_amount_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void addFundsToBalance(double amount) {
        double currentBalance = Auth.signedInUser.getBalance();
        double newBalance = currentBalance + amount;
        
        Auth.signedInUser.setBalance(newBalance);
        
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Crowdia").child("Users");
        usersRef.child(Auth.signedInUser.getKey()).child("balance").setValue(newBalance);
        
        // обновляем отображение баланса
        balanceText.setText(String.format("%.0f ₸", newBalance));
        
        Toast.makeText(getActivity(), getString(R.string.funds_added_success, (int)amount), Toast.LENGTH_SHORT).show();
    }

    private void showManageUsersDialog() {
        Intent intent = new Intent(getActivity(), AdminUsersActivity.class);
        startActivity(intent);
    }

    private void showManageProjectsDialog() {
        Intent intent = new Intent(getActivity(), AdminProjectsActivity.class);
        startActivity(intent);
    }
}