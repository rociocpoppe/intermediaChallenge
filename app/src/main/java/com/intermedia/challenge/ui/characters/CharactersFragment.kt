package com.intermedia.challenge.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.intermedia.challenge.databinding.FragmentCharactersBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CharactersFragment : Fragment() {

    private lateinit var binding: FragmentCharactersBinding
    private val viewModel: CharactersViewModel by sharedViewModel()
    private val adapter = CharactersAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCharactersBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharactersList()
        setupPagination()
    }

    private fun setupPagination() {
        binding.listCharacters.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadMoreCharacters()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun setupCharactersList() {
        adapter.onClickListener = { character ->
            // TODO complete
        }
        binding.listCharacters.adapter = adapter
        viewModel.characters.observe(viewLifecycleOwner, { characters ->
            // TODO fill adapter items
        })
    }
}