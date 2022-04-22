package com.mcris.localexchange.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mcris.localexchange.databinding.FragmentCategoriesSelectionBinding;
import com.mcris.localexchange.models.CategoriesAdapter;
import com.mcris.localexchange.models.ClickableAdapterListener;
import com.mcris.localexchange.models.entities.Category;
import com.mcris.localexchange.viewmodels.MainViewModel;

import java.util.List;


public class CategoriesSelectionFragment extends Fragment {

    private FragmentCategoriesSelectionBinding binding;
    private MainViewModel mainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriesSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Category> categories = mainViewModel.getCategories();
        CategoriesAdapter adapter = new CategoriesAdapter(categories);
        adapter.setOnClickListener(new ClickableAdapterListener<Category>() {
            @Override
            public void onListItemClick(Category item, int position) {

            }
        });

        binding.categoriesRecyclerView.setHasFixedSize(true);
        binding.categoriesRecyclerView.setAdapter(adapter);
        binding.categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}