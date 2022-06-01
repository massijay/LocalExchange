package com.mcris.localexchange.views;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

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

    private CategoriesAdapter categoriesAdapter;

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

        List<Category> categories = mainViewModel.getAllCategories();
        categoriesAdapter = new CategoriesAdapter(categories);
        categoriesAdapter.setOnClickListener(new ClickableAdapterListener<Category>() {
            @Override
            public void onListItemClick(Category item, int position) {

            }
        });

        binding.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    v.clearFocus();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).hideSoftKeyboard(v);
                    }
                    return true;
                }
                return false;
            }
        });

        binding.searchItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.searchEditText.clearFocus();
                search();
            }
        });
        binding.categoriesRecyclerView.setHasFixedSize(true);
        binding.categoriesRecyclerView.setAdapter(categoriesAdapter);
        binding.categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void search() {
        String text = binding.searchEditText.getText().toString().trim();
        if (text.length() == 0 || text.length() > 2) {
            mainViewModel.getObservableItems().clear();
            mainViewModel.setSearchText(text);
            Category c = categoriesAdapter.getSelectedCategory();
            mainViewModel.setSelectedCategoryId(c != null ? c.getId() : null);
            mainViewModel.downloadItems();
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).collapseBottomSheet();
        }
    }
}