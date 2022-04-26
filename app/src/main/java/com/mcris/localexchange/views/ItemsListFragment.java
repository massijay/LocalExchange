package com.mcris.localexchange.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableMap;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.model.LatLng;
import com.mcris.localexchange.databinding.FragmentItemsListBinding;
import com.mcris.localexchange.models.ItemsAdapter;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.viewmodels.MainViewModel;

import java.util.Collection;

public class ItemsListFragment extends Fragment {

    private FragmentItemsListBinding binding;
    private MainViewModel mainViewModel;

    private ItemsAdapter itemsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemsAdapter = new ItemsAdapter();

        itemsAdapter.setOnClickListener((item, position) -> {
            // View item details page
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).focusItemOnMap(item);
            }
        });

        binding.mainRecyclerView.setHasFixedSize(true);
        binding.mainRecyclerView.setAdapter(itemsAdapter);
        binding.mainRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mainViewModel.getUserLocation()
                .observe(getViewLifecycleOwner(), latLng -> setCurrentLocationInAdapter(latLng));
        setCurrentLocationInAdapter(mainViewModel.getUserLocation().getValue());

        addItemsToList(mainViewModel.getObservableItems().values());
        mainViewModel.getObservableItems().addOnMapChangedCallback(
                new ObservableMap.OnMapChangedCallback<ObservableMap<String, Item>, String, Item>() {
                    @Override
                    public void onMapChanged(ObservableMap<String, Item> sender, String key) {
                        if (sender.size() == 0) clearItemsInList();
                        if (key != null) addItemToList(sender.get(key));
                    }
                });
    }

    private void setCurrentLocationInAdapter(LatLng latLng) {
        itemsAdapter.setReferencePosition(latLng);
    }

    private void addItemToList(Item item) {
        itemsAdapter.addItem(item);
    }

    private void addItemsToList(Collection<Item> items) {
        itemsAdapter.addItems(items);
    }

    private void clearItemsInList() {
        itemsAdapter.clearItems();
    }
}