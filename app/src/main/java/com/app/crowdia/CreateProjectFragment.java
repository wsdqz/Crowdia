package com.app.crowdia;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import android.util.Base64;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import classes.Project;

public class CreateProjectFragment extends Fragment {
    private EditText titleInput, descriptionInput, goalAmountInput, deadlineInput;
    private AutoCompleteTextView categoryInput;
    private Button saveButton;
    private LinearLayout additionalImagesContainer;
    private Calendar deadlineCalendar = Calendar.getInstance();
    
    private static final int REQUEST_CAMERA_IMAGE = 1;
    private static final int REQUEST_GALLERY_IMAGE = 2;
    
    private ArrayList<String> imagesBase64 = new ArrayList<>();
    private ArrayList<CardView> imageCards = new ArrayList<>();
    private static final int MAX_IMAGES = 8;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_project, container, false);
        
        titleInput = view.findViewById(R.id.titleInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        goalAmountInput = view.findViewById(R.id.goalAmountInput);
        deadlineInput = view.findViewById(R.id.deadlineInput);
        categoryInput = view.findViewById(R.id.categoryInput);
        saveButton = view.findViewById(R.id.saveButton);
        additionalImagesContainer = view.findViewById(R.id.additionalImagesContainer);
        CardView addImageCard = view.findViewById(R.id.addImageCard);
        
        // список категорий
        String[] categories = {"Технологии", "Искусство", "Спорт", "Образование", "Еда", "Игры", "Мода", "Музыка", "Кино", "Литература", "Фотография", "Дизайн", "Наука", "Благотворительность", "Бизнес", "Путешествия", "Экология", "Здоровье", "Другое"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryInput.setAdapter(adapter);
        
        // обработчик нажатия для открытия диалога выбора категории
        categoryInput.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.choose_category_title);
            builder.setItems(categories, (dialog, which) -> {
                categoryInput.setText(categories[which]);
            });
            builder.show();
        });
        
        // выбор дедлайна
        deadlineInput.setOnClickListener(v -> showDateTimePickers());
        
        // добавление изображений
        addImageCard.setOnClickListener(v -> {
            if (imagesBase64.size() < MAX_IMAGES) {
                showImagePickerDialog();
            } else {
                Toast.makeText(getActivity(), getString(R.string.max_images_reached, MAX_IMAGES), Toast.LENGTH_SHORT).show();
            }
        });
        
        // сохранение проекта
        saveButton.setOnClickListener(v -> saveProject());
        
        return view;
    }
    
    private void showImagePickerDialog() {
        String[] options = new String[]{getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_image);
        
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // камера
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CAMERA_IMAGE);
            } else if (which == 1) { // галерея
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
            }
        });
        
        AlertDialog alertDialog = builder.show();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    
    private void showDateTimePickers() {
        Calendar currentDate = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    deadlineCalendar.set(Calendar.YEAR, year);
                    deadlineCalendar.set(Calendar.MONTH, month);
                    deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // после выбора даты показываем диалог выбора времени
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                            (view1, hourOfDay, minute) -> {
                                deadlineCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                deadlineCalendar.set(Calendar.MINUTE, minute);
                                
                                // форматируем и отображаем выбранную дату и время
                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                                deadlineInput.setText(sdf.format(deadlineCalendar.getTime()));
                            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH));
        
        // минимальная дата (сегодня)
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
        datePickerDialog.show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == getActivity().RESULT_OK && data != null) {
            // обработка результата от камеры
            if (requestCode == REQUEST_CAMERA_IMAGE) {
                try {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap == null) {
                        Toast.makeText(getActivity(), R.string.camera_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    String imageBase64 = bitmapToBase64(bitmap);
                    if (imageBase64 == null || imageBase64.isEmpty()) {
                        Toast.makeText(getActivity(), R.string.encode_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    imagesBase64.add(imageBase64);
                    addImageToContainer(bitmap);
                    
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.image_loading_error, Toast.LENGTH_SHORT).show();
                }
            }
            // обработка результата из галереи
            else if (requestCode == REQUEST_GALLERY_IMAGE) {
                try {
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) {
                        Toast.makeText(getActivity(), R.string.gallery_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    InputStream is = getActivity().getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                    
                    if (bitmap == null) {
                        Toast.makeText(getActivity(), R.string.decode_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    String imageBase64 = bitmapToBase64(bitmap);
                    if (imageBase64 == null || imageBase64.isEmpty()) {
                        Toast.makeText(getActivity(), R.string.encode_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    imagesBase64.add(imageBase64);
                    addImageToContainer(bitmap);
                    
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.image_loading_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void addImageToContainer(Bitmap bitmap) {
        // новая карточка с изображением
        CardView card = new CardView(getActivity());
        card.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(100), dpToPx(100)));
        card.setRadius(dpToPx(8));
        card.setCardElevation(0);
        ((LinearLayout.LayoutParams) card.getLayoutParams()).setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        
        // контейнер для изображения и кнопки удаления
        RelativeLayout container = new RelativeLayout(getActivity());
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        // ImageView для отображения изображения
        ImageView imageView = new ImageView(getActivity());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
        container.addView(imageView);
        
        // кнопка удаления
        ImageView deleteButton = new ImageView(getActivity());
        RelativeLayout.LayoutParams deleteParams = new RelativeLayout.LayoutParams(dpToPx(24), dpToPx(24));
        deleteParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        deleteParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        deleteParams.setMargins(0, dpToPx(4), dpToPx(4), 0);
        deleteButton.setLayoutParams(deleteParams);
        deleteButton.setImageResource(android.R.drawable.ic_delete);
        deleteButton.setBackground(getResources().getDrawable(R.drawable.circle_background));
        deleteButton.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        deleteButton.setColorFilter(android.graphics.Color.RED);
        container.addView(deleteButton);
        card.addView(container);
        
        // индекс изображения
        final int position = imageCards.size();
        
        // обработчик нажатия на кнопку удаления
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_image)
                .setMessage(R.string.delete_image_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // удаление карточки из контейнера
                    additionalImagesContainer.removeView(card);
                    
                    // удаление изображения из списков
                    if (position < imagesBase64.size()) {
                        imagesBase64.remove(position);
                    }
                    
                    // удаление карточки из списка
                    imageCards.remove(card);
                    
                    // обновление индексов для всех оставшихся карточек
                    for (int i = 0; i < imageCards.size(); i++) {
                        CardView remainingCard = imageCards.get(i);
                        remainingCard.setTag(i);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
        });
        
        // сохранение позиции в теге карточки
        card.setTag(position);
        
        // добавление карточки в контейнер перед кнопкой добавления
        imageCards.add(card);
        additionalImagesContainer.addView(card, additionalImagesContainer.getChildCount() - 1);
    }
    
    private int dpToPx(int dp) {
        float density = getActivity().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    
    private String bitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            
            if (encoded == null || encoded.isEmpty()) {
                return null;
            }

            return encoded;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void saveProject() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String goalAmountStr = goalAmountInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        
        // проверка обязательных полей
        if (title.isEmpty() || description.isEmpty() || goalAmountStr.isEmpty() || category.isEmpty() || deadlineInput.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // проверка наличия хотя бы одного изображения
        if (imagesBase64.isEmpty()) {
            Toast.makeText(getActivity(), R.string.add_at_least_one_image, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // проверка что все изображения закодированы корректно
        boolean hasInvalidImages = false;
        for (int i = 0; i < imagesBase64.size(); i++) {
            String imageBase64 = imagesBase64.get(i);
            if (imageBase64 == null || imageBase64.isEmpty()) {
                hasInvalidImages = true;
            }
        }
        
        if (hasInvalidImages) {
            Toast.makeText(getActivity(), R.string.invalid_images, Toast.LENGTH_LONG).show();
            return;
        }
        
        // предупреждение о невозможности редактирования после создания
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.warning_title)
            .setMessage(R.string.warning_message)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                // создание проекта
                proceedWithProjectCreation(title, description, goalAmountStr, category);
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }
    
    private void proceedWithProjectCreation(String title, String description, String goalAmountStr, String category) {
        try {
            // некликабельная кнопка
            saveButton.setEnabled(false);
            saveButton.setText(R.string.saving_button);
            
            double goalAmount = Double.parseDouble(goalAmountStr);
            long deadlineTimestamp = deadlineCalendar.getTimeInMillis();
            
            Project project = new Project();
            project.setTitle(title);
            project.setDescription(description);
            project.setCategory(category);
            project.setGoalAmount(goalAmount);
            project.setCurrentAmount(0);
            project.setAvailableAmount(0);
            project.setDeadline(deadlineTimestamp);
            project.setCreatorId(classes.Auth.signedInUser.getKey());
            project.setCreatedAt(System.currentTimeMillis());
            
            // первое изображение - обложка
            if (!imagesBase64.isEmpty()) {
                project.setCoverImage(imagesBase64.get(0));
                
                // остальные изображения как дополнительные
                for (int i = 1; i < imagesBase64.size(); i++) {
                    project.addAdditionalImage(imagesBase64.get(i));
                }
            }
            
            Toast.makeText(getActivity(), R.string.saving_project, Toast.LENGTH_SHORT).show();
            
            // сохраняем в Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference projectRef = database.getReference("Crowdia").child("Projects");
            DatabaseReference newProjectRef = projectRef.push();
            String key = newProjectRef.getKey();
            project.setKey(key);
            
            newProjectRef.setValue(project).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // добавляем ключ проекта в список проектов пользователя
                    DatabaseReference usersRef = database.getReference("Crowdia").child("Users");
                    DatabaseReference userRef = usersRef.child(classes.Auth.signedInUser.getKey());

                    // получаем текущий список проектов пользователя или создаем новый
                    ArrayList<String> userProjects = classes.Auth.signedInUser.getProjects();
                    if (userProjects == null) {
                        userProjects = new ArrayList<>();
                    }
                    
                    // добавляем новый проект в список
                    userProjects.add(key);

                    // обновляем список проектов в объекте пользователя
                    classes.Auth.signedInUser.setProjects(userProjects);
                    
                    // обновляем поле projects у пользователя в Firebase
                    userRef.child("projects").setValue(userProjects).addOnCompleteListener(userTask -> {
                        saveButton.setEnabled(true);
                        saveButton.setText(R.string.save_button);
                        
                        if (userTask.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.project_created, Toast.LENGTH_SHORT).show();
                            clearForm();
                            
                            try {
                                // переход на "Мои проекты"
                                Intent intent = new Intent(getActivity(), UserProjectsActivity.class);
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), R.string.project_created_navigation_error, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.project_created_not_added, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        // восстанавление кнопки в случае ошибки
                        saveButton.setEnabled(true);
                        saveButton.setText(R.string.save_button);
                        Toast.makeText(getActivity(), getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    saveButton.setEnabled(true);
                    saveButton.setText(R.string.save_button);
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "";
                    Toast.makeText(getActivity(), getString(R.string.error_creating_project, errorMessage), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                saveButton.setEnabled(true);
                saveButton.setText(R.string.save_button);
                Toast.makeText(getActivity(), getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show();
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), R.string.enter_valid_amount_hint, Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText(R.string.save_button);
        } catch (Exception e) {
            Toast.makeText(getActivity(), getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText(R.string.save_button);
        }
    }
    
    private void clearForm() {
        titleInput.setText("");
        descriptionInput.setText("");
        goalAmountInput.setText("");
        categoryInput.setText("");
        deadlineInput.setText("");
        
        // очищаем изображения
        for (CardView card : imageCards) {
            additionalImagesContainer.removeView(card);
        }
        imageCards.clear();
        imagesBase64.clear();
    }
} 