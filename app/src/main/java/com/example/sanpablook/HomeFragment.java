package com.example.sanpablook;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import com.example.sanpablook.Adapter.RecyclerConfirmed;
import com.example.sanpablook.Adapter.RecyclerPastWeek;
import com.example.sanpablook.Adapter.RecyclerPending;
import com.example.sanpablook_establishment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class HomeFragment extends Fragment {

    CalendarView calendarView;
    Calendar calendar;

    FirebaseFirestore db;
    Button btnAccept;
    RecyclerView recyclerViewConfirmed, recyclerViewNextWeek, recyclerViewPastWeek, recyclerViewPending;

    String todayDate, oneWeekAgoDate, oneWeekAheadDate;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        //CALENDAR VIEW
        calendarView = view.findViewById(R.id.calendarDashboard);
        calendar = Calendar.getInstance();
        calendar.getMinimalDaysInFirstWeek();
        Calendar oneWeekAgo = Calendar.getInstance();
        oneWeekAgo.add(Calendar.WEEK_OF_YEAR, -1);
        Calendar oneWeekAhead = Calendar.getInstance();
        oneWeekAhead.add(Calendar.WEEK_OF_YEAR, 1);


        //sdf
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault());
        String todayDate = sdf.format(calendar.getTime());
        String oneWeekAgoDate = sdf.format(oneWeekAgo.getTime());
        String oneWeekAheadDate = sdf.format(oneWeekAhead.getTime());



        //RECYCLER VIEW
        recyclerViewConfirmed = view.findViewById(R.id.recyclerViewConfirmed);
        recyclerViewConfirmed.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewNextWeek = view.findViewById(R.id.recyclerViewNextWeek);
        recyclerViewNextWeek.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewPastWeek = view.findViewById(R.id.recyclerViewPastWeek);
        recyclerViewPastWeek.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewPending = view.findViewById(R.id.recyclerViewPending);
        recyclerViewPending.setLayoutManager(new LinearLayoutManager(requireContext()));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                // Create a Calendar object with the selected date
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, day);

                // SimpleDateForm
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault());

                // Converts selected date to string
                String selectedDate = sdf.format(selectedCalendar.getTime());

                // Log the selected date
                Log.d("HomeFragment", "Selected date: " + selectedDate);

                // Fetch the bookings from the database where the date equals the selected date
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("BookingPending")
                        .whereEqualTo("status", "Pending")
                        .whereEqualTo("establishmentID", "casaDine")
                        .whereEqualTo("date", selectedDate)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<Map<String, Object>> bookings = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        bookings.add(document.getData());
                                    }
                                    // Initialize the adapter
                                    RecyclerPending adapter = new RecyclerPending(bookings);

                                    // Set the adapter to the RecyclerView
                                    recyclerViewPending.setAdapter(adapter);
                                } else {
                                    Log.d("HomeFragment", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        //Query Confirmed
        db.collection("BookingPending")
                .whereEqualTo("status", "Confirmed")
                .whereEqualTo("establishmentID", "casaDine")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookings.add(document.getData());
                        }
                        // Log the size of the bookings list
                        Log.d("HomeFragment", "Number of documents fetched for Confirmed: " + bookings.size());

                        // Initialize the adapter
                        RecyclerConfirmed adapter = new RecyclerConfirmed(bookings);

                        // Set the adapter to the RecyclerView
                        recyclerViewConfirmed.setAdapter(adapter);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });

        //Query Date base on next week
        db.collection("BookingPending")
                .whereEqualTo("establishmentID", "casaDine")
                .whereGreaterThanOrEqualTo("date", todayDate)
                .whereLessThanOrEqualTo("date", oneWeekAheadDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookings.add(document.getData());
                        }
                        // Log the size of the bookings list
                        Log.d("HomeFragment", "Number of documents fetched for oneWeekAhead: " + bookings.size());

                        // Initialize the adapter
                        RecyclerPastWeek adapter = new RecyclerPastWeek(bookings);

                        // Set the adapter to the RecyclerView
                        recyclerViewNextWeek.setAdapter(adapter);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });

        // Query Date base on last week
        db.collection("BookingPending")
                .whereEqualTo("establishmentID", "casaDine")
                .whereLessThanOrEqualTo("date", todayDate)
                .whereGreaterThanOrEqualTo("date", oneWeekAgoDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookings.add(document.getData());
                        }
                        // Log the size of the bookings list
                        Log.d("HomeFragment", "Number of documents fetched for oneWeekAgo: " + bookings.size());

                        // Initialize the adapter
                        RecyclerPastWeek adapter = new RecyclerPastWeek(bookings);

                        // Set the adapter to the RecyclerView
                        recyclerViewPastWeek.setAdapter(adapter);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });

        // Query all pending booking
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("BookingPending")
                .whereEqualTo("status", "Pending")
                .whereEqualTo("establishmentID", "casaDine")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Map<String, Object>> bookings = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                bookings.add(document.getData());
                            }
                            // Initialize the adapter
                            RecyclerPending adapter = new RecyclerPending(bookings);

                            // Set the adapter to the RecyclerView
                            recyclerViewPending.setAdapter(adapter);
                        } else {
                            Log.d("HomeFragment", "Error getting documents: ", task.getException());
                        }
                    }
                });





        //BUTTON TEST TO REGISTRATION COMPLETE
//        btnAccept = view.findViewById(R.id.btnAccept);
//        btnAccept.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                goToRegistrationCompleteActivity(view);
//                goToRegistrationCompleteActivity(view);
//                Toast.makeText(getContext(), "Successful Booking", Toast.LENGTH_SHORT).show();
//            }
//
//            private void goToRegistrationCompleteActivity(View view) {
////                Intent intent = new Intent(getActivity(), RegistrationCompleteActivity.class);
////                startActivity(intent);
//            }
//        });

        return view;
    }

    public void refreshData() {
        // Query all pending booking
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("BookingPending")
                .whereEqualTo("status", "Pending")
                .whereEqualTo("establishmentID", "casaDine")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookings.add(document.getData());
                        }
                        // Initialize the adapter
                        RecyclerPending adapter = new RecyclerPending(bookings);

                        // Set the adapter to the RecyclerView
                        recyclerViewPending.setAdapter(adapter);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });
        //Query Confirmed
        db.collection("BookingPending")
                .whereEqualTo("status", "Confirmed")
                .whereEqualTo("establishmentID", "casaDine")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookings.add(document.getData());
                        }
                        // Log the size of the bookings list
                        Log.d("HomeFragment", "Number of documents fetched for Confirmed: " + bookings.size());

                        // Initialize the adapter
                        RecyclerConfirmed adapter = new RecyclerConfirmed(bookings);

                        // Set the adapter to the RecyclerView
                        recyclerViewConfirmed.setAdapter(adapter);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });

        //Query Date base on next week
        db.collection("BookingPending")
                .whereEqualTo("establishmentID", "casaDine")
                .whereGreaterThanOrEqualTo("date", todayDate)
                .whereLessThanOrEqualTo("date", oneWeekAheadDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookings.add(document.getData());
                        }
                        // Log the size of the bookings list
                        Log.d("HomeFragment", "Number of documents fetched for oneWeekAhead: " + bookings.size());

                        // Initialize the adapter
                        RecyclerPastWeek adapter = new RecyclerPastWeek(bookings);

                        // Set the adapter to the RecyclerView
                        recyclerViewNextWeek.setAdapter(adapter);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });

        // Query Date base on last week
        db.collection("BookingPending")
                .whereEqualTo("establishmentID", "casaDine")
                .whereLessThanOrEqualTo("date", todayDate)
                .whereGreaterThanOrEqualTo("date", oneWeekAgoDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            bookings.add(document.getData());
                        }
                        // Log the size of the bookings list
                        Log.d("HomeFragment", "Number of documents fetched for oneWeekAgo: " + bookings.size());

                        // Initialize the adapter
                        RecyclerPastWeek adapter = new RecyclerPastWeek(bookings);

                        // Set the adapter to the RecyclerView
                        recyclerViewPastWeek.setAdapter(adapter);
                    } else {
                        Log.d("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });


    }

    public void setDate(int day, int month, int year){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        long milli = calendar.getTimeInMillis();
        calendarView.setDate(milli);
    }


}