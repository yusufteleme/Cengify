import java.util.ArrayList;

public class PlaylistTree {
	
	public PlaylistNode primaryRoot;		//root of the primary B+ tree
	public PlaylistNode secondaryRoot;	//root of the secondary B+ tree
	public PlaylistTree(Integer order) {
		PlaylistNode.order = order;
		primaryRoot = new PlaylistNodePrimaryLeaf(null);
		primaryRoot.level = 0;
		secondaryRoot = new PlaylistNodeSecondaryLeaf(null);
		secondaryRoot.level = 0;
	}
	
	public void addSong(CengSong song) {
		// TODO: Implement this method
		// add methods to fill both primary and secondary tree

		primaryAdd(song);
		secondaryAdd(song);

		return;
	}
	

	public CengSong searchSong(Integer audioId) {
		// TODO: Implement this method
		// find the song with the searched audioId in primary B+ tree
		// return value will not be tested, just print according to the specifications


		if(primaryRoot.getType() == PlaylistNodeType.Leaf){
			PlaylistNodePrimaryLeaf leaf = (PlaylistNodePrimaryLeaf) primaryRoot;
			System.out.println("<data>");
			System.out.print("<record>");
			for(int i=0; i<leaf.songCount(); i++)
				if(audioId == leaf.audioIdAtIndex(i))
					System.out.print(leaf.songAtIndex(i).fullName());
			System.out.println("</record>");
			System.out.println("</data>");
			return null;
		}

		PlaylistNodePrimaryIndex parent = (PlaylistNodePrimaryIndex) primaryRoot;
		PlaylistNodePrimaryIndex newParent = (PlaylistNodePrimaryIndex) primaryRoot;
		PlaylistNodePrimaryLeaf leaf = new PlaylistNodePrimaryLeaf(null);

		while(leaf.getParent()==null){
			parent = newParent;
			int tabCount = parent.level;
			for(int j=0; j<tabCount; j++)
				System.out.print("\t");
			System.out.println("<index>");

			for(int i=0; i<parent.audioIdCount(); i++){
				for(int j=0; j<tabCount; j++)
					System.out.print("\t");
				System.out.println(parent.audioIdAtIndex(i));
			}

			for (int i = 0; i < parent.audioIdCount(); i++){
				if(audioId < parent.audioIdAtIndex(i)){
					if(parent.getChildrenAt(i).getType() == PlaylistNodeType.Leaf){
						leaf = (PlaylistNodePrimaryLeaf) parent.getChildrenAt(i);
						break;
					}
					else{
						newParent = (PlaylistNodePrimaryIndex) parent.getChildrenAt(i);
						break;
					}
				}
				else if(i == (parent.audioIdCount() -1)){
					if(parent.getChildrenAt(i+1).getType() == PlaylistNodeType.Leaf){
						leaf = (PlaylistNodePrimaryLeaf) parent.getChildrenAt(i+1);
						break;
					}
					else{
						newParent = (PlaylistNodePrimaryIndex) parent.getChildrenAt(i+1);
						break;
					}
				}
			}
			for(int j=0; j<tabCount; j++)
				System.out.print("\t");
			System.out.println("</index>");

			if(leaf.getParent() != null){
				for(int i=0; i<leaf.level; i++)
					System.out.print("\t");
				System.out.println("<data>");
				for(int i=0; i<leaf.level; i++)
					System.out.print("\t");
				System.out.print("<record>");
				for(int i=0; i<leaf.songCount(); i++)
					if(audioId == leaf.audioIdAtIndex(i))
						System.out.print(leaf.songAtIndex(i).fullName());
				System.out.println("</record>");
				for(int i=0; i<leaf.level; i++)
					System.out.print("\t");
				System.out.println("</data>");
			}
		}

		return null;
	}
	
	
	public void printPrimaryPlaylist() {
		// TODO: Implement this method
		// print the primary B+ tree in Depth-first order

		printPrimaryHelper(primaryRoot);

		return;
	}
	
	public void printSecondaryPlaylist() {
		// TODO: Implement this method
		// print the secondary B+ tree in Depth-first order

		printSecondaryHelper(secondaryRoot);

		return;
	}
	
	// Extra functions if needed

	
	private void primaryAdd(CengSong song){
		
		int order = PlaylistNode.order;
		int audioId = song.audioId();

		PlaylistNodePrimaryIndex parent = new PlaylistNodePrimaryIndex(null);
		PlaylistNodePrimaryLeaf leaf = new PlaylistNodePrimaryLeaf(null);

		// Search for location of needed leaf and it's parent
		if(primaryRoot.getType() == PlaylistNodeType.Internal){
			parent = (PlaylistNodePrimaryIndex) primaryRoot;
			while(leaf.getParent()==null){
				for(int i=0; i<parent.songCount(); i++){
					if(audioId < parent.audioIdAtIndex(i)){
						if(parent.getChildrenAt(i).getType() == PlaylistNodeType.Internal){
							parent = (PlaylistNodePrimaryIndex) parent.getChildrenAt(i);
							break;
						}
						else{
							leaf = (PlaylistNodePrimaryLeaf) parent.getChildrenAt(i);
							break;
						}
					}
					else if(i == parent.songCount()-1){
						if(parent.getChildrenAt(i+1).getType() == PlaylistNodeType.Internal){
							parent = (PlaylistNodePrimaryIndex) parent.getChildrenAt(i+1);
							break;
						}
						else{
							leaf = (PlaylistNodePrimaryLeaf) parent.getChildrenAt(i+1);
							break;
						}
					}
				}
			}
		}
		else
			leaf = (PlaylistNodePrimaryLeaf) primaryRoot;

		// Adding song to leaf
		if(leaf.songCount()==0)
			leaf.addSong(0, song);
		else{
			for(int i=0; i<leaf.songCount(); i++){
				if(audioId < leaf.audioIdAtIndex(i)){
					leaf.addSong(i, song);
					break;
				}
				else if(i==leaf.songCount()-1){
					leaf.addSong(i+1, song);
					break;
				}
			}
		}
		// Overflow copy_up
		if(leaf.songCount() > 2*order){
			ArrayList<PlaylistNode> splittedLeaves = splitLeafPrimary(leaf);
			PlaylistNodePrimaryLeaf first = (PlaylistNodePrimaryLeaf) splittedLeaves.get(0);
			PlaylistNodePrimaryLeaf second = (PlaylistNodePrimaryLeaf) splittedLeaves.get(1);
			
			int middleId = second.songAtIndex(0).audioId();
			int insertChild = 0;

			if(leaf.getParent()==null){
				PlaylistNodePrimaryIndex root = new PlaylistNodePrimaryIndex(null);
				root.addAudioId(0, middleId);
				first.setParent(root); second.setParent(root);
				root.addChild(0, first); root.addChild(1, second);
				primaryRoot = root;
			}
			else{
				for(int i=0; i<parent.songCount(); i++){
					if(middleId < parent.audioIdAtIndex(i)){
						parent.addAudioId(i, middleId);
						insertChild = i;
						break;
					}
					else if(i == parent.songCount()-1){
						parent.addAudioId(i+1, middleId);
						insertChild = i+1;
						break;
					}
				}

				PlaylistNode root = pushUpPrimary(primaryRoot, parent, insertChild, first, second);
				primaryRoot = root;
			}
		}

	}

	private void secondaryAdd(CengSong song){
		
		int order = PlaylistNode.order;
		PlaylistNodeSecondaryIndex parent = new PlaylistNodeSecondaryIndex(null);
		PlaylistNodeSecondaryLeaf leaf = new PlaylistNodeSecondaryLeaf(null);
		
		int audioId = song.audioId();
		String genre = song.genre();

		if(secondaryRoot.getType() == PlaylistNodeType.Internal){

			parent =  (PlaylistNodeSecondaryIndex) secondaryRoot;

			// Find necessary leaf
			while(leaf.getParent() == null){
				for(int i=0; i<parent.genreCount(); i++){
					
					if(genre.compareTo(parent.genreAtIndex(i)) < 0 ){
						if(parent.getChildrenAt(i).getType() == PlaylistNodeType.Internal){
							parent = (PlaylistNodeSecondaryIndex) parent.getChildrenAt(i); break;
						}
						else{
							leaf = (PlaylistNodeSecondaryLeaf) parent.getChildrenAt(i); break;
						}
					}
					else if(i == parent.genreCount()-1){
						if(parent.getChildrenAt(i+1).getType() == PlaylistNodeType.Internal){
							parent = (PlaylistNodeSecondaryIndex) parent.getChildrenAt(i+1); break;
						}
						else{
							leaf = (PlaylistNodeSecondaryLeaf) parent.getChildrenAt(i+1); break;
						}
					}
				}
			}
		}
		else
			leaf = (PlaylistNodeSecondaryLeaf) secondaryRoot;

		// Add to that leaf
		if(leaf.genreCount() == 0)
			leaf.addSong(0, song);
		else{
			for(int i=0; i<leaf.genreCount(); i++){
				if(genre.compareTo(leaf.genreAtIndex(i)) <= 0 ){
					leaf.addSong(i, song); break;
				}
				else if(i == leaf.genreCount()-1){
					leaf.addSong(i+1, song); break;
				}
			}
		}

		// Chcek for overflow
		// TODO
		if(leaf.genreCount() > 2*order){
			ArrayList<PlaylistNode> splittedLeaves = splitLeafSecondary(leaf);
			PlaylistNodeSecondaryLeaf first = (PlaylistNodeSecondaryLeaf) splittedLeaves.get(0);
			PlaylistNodeSecondaryLeaf second = (PlaylistNodeSecondaryLeaf) splittedLeaves.get(1);
			String middleGenre = second.genreAtIndex(0);
			parent = (PlaylistNodeSecondaryIndex) leaf.getParent();
			int insertChild = 0;
			if(parent==null){ // Root
				PlaylistNodeSecondaryIndex root = new PlaylistNodeSecondaryIndex(null);
				root.addGenre(0, middleGenre);
				first.setParent(root);
				second.setParent(root);
				root.addChild(0, first);
				root.addChild(1, second);
				secondaryRoot = root;
			}
			else{
				for(int i=0; i<parent.genreCount(); i++){
					if(middleGenre.compareTo(parent.genreAtIndex(i)) < 0){
						insertChild = i;
						parent.addGenre(insertChild, middleGenre);
						break;
					}
					else if(i==parent.genreCount()-1){
						insertChild = i+1;
						parent.addGenre(insertChild, middleGenre);
						break;
					}
				}

				PlaylistNode root  = pushUpSecondary(secondaryRoot, parent, insertChild, first, second);
				secondaryRoot = root;
			}
		}
	
		return;
	}

	private ArrayList<PlaylistNode> splitLeafPrimary(PlaylistNode leafToSplit){
		
		PlaylistNodePrimaryLeaf leaf = (PlaylistNodePrimaryLeaf) leafToSplit;
		int order = PlaylistNode.order;
		ArrayList<CengSong> first = new ArrayList<CengSong>();
		ArrayList<CengSong> second = new ArrayList<CengSong>();
		
		int songCount = leaf.songCount();
		for (int i = 0; i < songCount; i++) {
			if(i < order)
				first.add(leaf.songAtIndex(i));
			else
				second.add(leaf.songAtIndex(i));
		}

		PlaylistNodePrimaryLeaf first_leaf = new PlaylistNodePrimaryLeaf(null);
		PlaylistNodePrimaryLeaf second_leaf = new PlaylistNodePrimaryLeaf(null);

		int firstSize = first.size(), secondSize = second.size();
		for (int i = 0; i < firstSize; i++) 
			first_leaf.addSong(i, first.get(i));

		for (int i = 0; i < secondSize; i++) 
			second_leaf.addSong(i, second.get(i));

		ArrayList<PlaylistNode> result = new ArrayList<>();
		result.add(first_leaf);
		result.add(second_leaf);

		return result;
	}

	private ArrayList<PlaylistNode> splitLeafSecondary(PlaylistNode leafToSplit){
		
		PlaylistNodeSecondaryLeaf leaf = (PlaylistNodeSecondaryLeaf) leafToSplit;
		int order = PlaylistNode.order;
		ArrayList<ArrayList<CengSong>> first = new ArrayList<>();
		ArrayList<ArrayList<CengSong>> second = new ArrayList<>();
		
		int genreCount = leaf.genreCount();
		for (int i = 0; i < genreCount; i++) {
			if(i < order)
				first.add(leaf.songsAtIndex(i));
			else
				second.add(leaf.songsAtIndex(i));
		}

		PlaylistNodeSecondaryLeaf first_leaf = new PlaylistNodeSecondaryLeaf(null);
		PlaylistNodeSecondaryLeaf second_leaf = new PlaylistNodeSecondaryLeaf(null);

		int firstSize = first.size(), secondSize = second.size();
		for (int i=0; i < firstSize; i++){
			int songsCountGenre = first.get(i).size(); 
			for(int j=0; j<songsCountGenre; j++)
				first_leaf.addSong(i, first.get(i).get(j));				
		}

		for (int i=0; i < secondSize; i++){
			int songsCountGenre = second.get(i).size(); 
			for(int j=0; j<songsCountGenre; j++)
				second_leaf.addSong(i, second.get(i).get(j));				
		}

		ArrayList<PlaylistNode> result = new ArrayList<>();
		result.add(first_leaf);
		result.add(second_leaf);

		return result;
	}

	private ArrayList<PlaylistNode> splitInternalPrimary(PlaylistNode internalToSplit, int insertChild, PlaylistNode firstLeaf, PlaylistNode secondLeaf){
		
		int order = PlaylistNode.order;
		PlaylistNodePrimaryIndex internal = (PlaylistNodePrimaryIndex) internalToSplit;
		ArrayList<Integer> indexOfFirstInternal = new ArrayList<>();
		ArrayList<Integer> indexOfSecondInternal = new ArrayList<>();
		ArrayList<PlaylistNode> childOfFirstInternal = new ArrayList<>();
		ArrayList<PlaylistNode> childOfSecondInternal = new ArrayList<>();


		boolean firstLeafFirstParent = true;
		boolean secondLeafFirstParent = true;

		for(int i=0; i<2*order+1; i++){
			if(i<order)
				indexOfFirstInternal.add(internal.audioIdAtIndex(i));
			else if(i>order)
				indexOfSecondInternal.add(internal.audioIdAtIndex(i));
		}

		int childIndex = 0;
		for(int i=0; i<2*order+2; i++){
			
			if(i<=order && i<insertChild)
				childOfFirstInternal.add(internal.getChildrenAt(childIndex++));
			
			else if(i<=order && i==insertChild){
				childOfFirstInternal.add(firstLeaf);
				firstLeafFirstParent = true;
			}
			
			else if(i<=order && i==insertChild+1){
				childOfFirstInternal.add(secondLeaf);
				childIndex++;
				secondLeafFirstParent = true;
			}
			else if(i<=order && i > insertChild+1)
				childOfFirstInternal.add(internal.getChildrenAt(childIndex++));

			else if(i>order && i<insertChild)
				childOfSecondInternal.add(internal.getChildrenAt(childIndex++));
			
			else if(i>order && i>insertChild+1)
				childOfSecondInternal.add(internal.getChildrenAt(childIndex++));
			
			else if(i>order && i==insertChild){
				childOfSecondInternal.add(firstLeaf);
				firstLeafFirstParent = false;
			}
			else if(i>order && i==insertChild+1){
				childOfSecondInternal.add(secondLeaf);
				childIndex++;
				secondLeafFirstParent = false;
			}
		}

		PlaylistNode newFirstInternal = new PlaylistNodePrimaryIndex(null, indexOfFirstInternal, childOfFirstInternal);
		PlaylistNode newSecondInternal = new PlaylistNodePrimaryIndex(null, indexOfSecondInternal, childOfSecondInternal);

		for (PlaylistNode child : ((PlaylistNodePrimaryIndex) newFirstInternal).getAllChildren())
			child.setParent(newFirstInternal);
		for (PlaylistNode child : ((PlaylistNodePrimaryIndex) newSecondInternal).getAllChildren())
			child.setParent(newSecondInternal);
		
		if(firstLeafFirstParent)
			firstLeaf.setParent(newFirstInternal);
		else
			firstLeaf.setParent(newSecondInternal);
		if(secondLeafFirstParent)
			secondLeaf.setParent(newFirstInternal);
		else
			secondLeaf.setParent(newSecondInternal);		
		
		ArrayList<PlaylistNode> newInternals = new ArrayList<>();
		newInternals.add(newFirstInternal);
		newInternals.add(newSecondInternal);
		
		return newInternals;
	}

	private ArrayList<PlaylistNode> splitInternalSecondary(PlaylistNode internalToSplit, int insertChild, PlaylistNode firstLeaf, PlaylistNode secondLeaf){
		
		int order = PlaylistNode.order;
		PlaylistNodeSecondaryIndex internal = (PlaylistNodeSecondaryIndex) internalToSplit;
		ArrayList<String> firstIndex = new ArrayList<>();
		ArrayList<String> secondIndex = new ArrayList<>();
		ArrayList<PlaylistNode> firstChildren = new ArrayList<>();
		ArrayList<PlaylistNode> secondChildren = new ArrayList<>();

		boolean firstLeafFirstParent = true;
		boolean secondLeafFirstParent = true;

		for(int i=0; i<2*order+1; i++){
			if(i<order)
				firstIndex.add(internal.genreAtIndex(i));
			else if(i>order)
				secondIndex.add(internal.genreAtIndex(i));
		}

		int childIndex = 0;
		for(int i=0; i<2*order+2; i++){

			if(i<order+1 && i< insertChild)
				firstChildren.add(internal.getChildrenAt(childIndex++));
			
			else if(i<order+1 && i==insertChild){
				firstChildren.add(firstLeaf);
				firstLeafFirstParent = true;
			}
			else if(i<order+1 && i==insertChild+1){
				firstChildren.add(secondLeaf);
				childIndex++;
				secondLeafFirstParent = true;
			}

			else if(i<order+1 && i>insertChild+1)
				firstChildren.add(internal.getChildrenAt(childIndex++));
			
			else if(i>order && i<insertChild)
				secondChildren.add(internal.getChildrenAt(childIndex++));

			else if(i>order && i>insertChild+1)
				secondChildren.add(internal.getChildrenAt(childIndex++));
				
			else if(i>order && i==insertChild){
				secondChildren.add(firstLeaf);
				firstLeafFirstParent = false;
			}

			else if(i>order && i==insertChild+1){
				secondChildren.add(secondLeaf);
				childIndex++;
				secondLeafFirstParent = false;
			}

		}

		PlaylistNode first = new PlaylistNodeSecondaryIndex(null, firstIndex, firstChildren);
		PlaylistNode second = new PlaylistNodeSecondaryIndex(null, secondIndex, secondChildren);

		for(PlaylistNode child : ((PlaylistNodeSecondaryIndex) first).getAllChildren())
			child.setParent(first);

		for(PlaylistNode child : ((PlaylistNodeSecondaryIndex) second).getAllChildren())
			child.setParent(second);

		ArrayList<PlaylistNode> result = new ArrayList<>();

		if(firstLeafFirstParent)
			firstLeaf.setParent(first);
		else
			firstLeaf.setParent(second);
		
		if(secondLeafFirstParent)
			secondLeaf.setParent(first);
		else
			secondLeaf.setParent(second);

		result.add(first);
		result.add(second);

		return result;
	}

	private PlaylistNode pushUpPrimary(PlaylistNode primaryRoot, PlaylistNode parentArg, int insertChild, PlaylistNode firstIndexArg, PlaylistNode secondIndexArg){

		int order = PlaylistNode.order;
		PlaylistNodePrimaryIndex firstIndex = new PlaylistNodePrimaryIndex(null);
		PlaylistNodePrimaryIndex secondIndex = new PlaylistNodePrimaryIndex(null);
		PlaylistNodePrimaryIndex root = new PlaylistNodePrimaryIndex(null);
		PlaylistNodePrimaryIndex parent = (PlaylistNodePrimaryIndex) parentArg;

		if(parent.songCount() <	2*order+1){ // NO PUSH_UP
			parent.removeChild(insertChild);
			firstIndexArg.setParent(parent); secondIndexArg.setParent(parent);
			parent.addChild(insertChild, firstIndexArg); parent.addChild(insertChild+1, secondIndexArg);
			return primaryRoot;
		}
		else if(parent.songCount() > 2*order){
			int pushUpId = parent.audioIdAtIndex(order);
			ArrayList<PlaylistNode> splittedInternals = splitInternalPrimary(parent, insertChild, firstIndexArg, secondIndexArg);
			firstIndex = (PlaylistNodePrimaryIndex) splittedInternals.get(0);
			secondIndex = (PlaylistNodePrimaryIndex) splittedInternals.get(1);
			
			if(parent.getParent()==null){
				root.addAudioId(0, pushUpId);
				firstIndex.setParent(root);
				secondIndex.setParent(root);
				root.addChild(0, firstIndex);
				root.addChild(1, secondIndex);
				return root;
			}
			else{
				parent = (PlaylistNodePrimaryIndex) parent.getParent();
				firstIndex.setParent(parent);
				secondIndex.setParent(parent);

				for(int i=0; i<parent.songCount(); i++){
					if(pushUpId < parent.audioIdAtIndex(i)){
						parent.addAudioId(i, pushUpId);
						insertChild = i;
						break;
					}
					else if(i==parent.songCount()-1){
						parent.addAudioId(i+1, pushUpId);
						insertChild = i+1;
						break;
					}
				}

				root = (PlaylistNodePrimaryIndex) pushUpPrimary(primaryRoot, parent, insertChild, firstIndex, secondIndex);
			}
		}

		return root;
	}

	private PlaylistNode pushUpSecondary(PlaylistNode secondaryRoot, PlaylistNode parentArg, int insertChild, PlaylistNode firstIndexArg, PlaylistNode secondIndexArg){
		int order = PlaylistNode.order;
		PlaylistNodeSecondaryIndex parent = (PlaylistNodeSecondaryIndex) parentArg;
		PlaylistNodeSecondaryIndex firstIndex = new PlaylistNodeSecondaryIndex(null);
		PlaylistNodeSecondaryIndex secondIndex = new PlaylistNodeSecondaryIndex(null);
		PlaylistNodeSecondaryIndex root = new PlaylistNodeSecondaryIndex(null);

		if(parent.genreCount() < 2*order+1){
			parent.removeChild(insertChild);
			firstIndexArg.setParent(parent); secondIndexArg.setParent(parent);
			parent.addChild(insertChild, firstIndexArg); parent.addChild(insertChild+1, secondIndexArg);
			return secondaryRoot;
		}
		else if(parent.genreCount() > 2*order){
			String pushUpGenre = parent.genreAtIndex(order);
			ArrayList<PlaylistNode> splittedInternals = splitInternalSecondary(parent, insertChild, firstIndexArg, secondIndexArg);
			firstIndex = (PlaylistNodeSecondaryIndex) splittedInternals.get(0);
			secondIndex = (PlaylistNodeSecondaryIndex) splittedInternals.get(1);

			if(parent.getParent()==null){
				root.addGenre(0, pushUpGenre);
				firstIndex.setParent(root);	secondIndex.setParent(root);
				root.addChild(0, firstIndex); root.addChild(1, secondIndex);
			}
			else{
				parent = (PlaylistNodeSecondaryIndex) parent.getParent();
				firstIndex.setParent(parent); secondIndex.setParent(parent);

				for(int i=0; i<parent.genreCount(); i++){
					if(pushUpGenre.compareTo(parent.genreAtIndex(i)) < 0){
						insertChild = i;
						parent.addGenre(insertChild, pushUpGenre);
						break;
					}
					else if(i==parent.genreCount()-1){
						insertChild = i+1;
						parent.addGenre(insertChild, pushUpGenre);
						break;
					}
				}

				root = (PlaylistNodeSecondaryIndex) pushUpSecondary(secondaryRoot, parentArg, insertChild, firstIndex, secondIndex);
			}

		}

		return root;
	}



	private void printPrimaryHelper(PlaylistNode nodeArg){

		int level = nodeArg.level;

		if(nodeArg.getType() == PlaylistNodeType.Internal){
			
			PlaylistNodePrimaryIndex node = (PlaylistNodePrimaryIndex) nodeArg;

			for(int i=0; i<level-1; i++)
				System.out.print("\t");
			
			System.out.println("<index>");

			for(int i=0; i<node.audioIdCount(); i++){
				for(int j=0; j<level-1; j++)
					System.out.print("\t");
				System.out.println(node.audioIdAtIndex(i));	
			}

			for(int i=0; i<level-1; i++)
				System.out.print("\t");
			System.out.println("</index>");

			for(int i=0; i<node.getAllChildren().size(); i++)
				printPrimaryHelper(node.getChildrenAt(i));

		}	
		else{
			PlaylistNodePrimaryLeaf node = (PlaylistNodePrimaryLeaf) nodeArg;

			for(int i=0; i<level-1; i++)
				System.out.print("\t");
			System.out.println("<data>");
			
			for(int i=0; i < node.songCount() ; i++){
			
				for(int j=0; j<level-1; j++)
					System.out.print("\t");
				System.out.print("<record>");

				System.out.print(node.songAtIndex(i).fullName());
				System.out.println("</record>");
			}

			for(int i=0; i<level-1; i++)
				System.out.print("\t");
			System.out.println("</data>");

		}

		return;
	}

	private void printSecondaryHelper(PlaylistNode nodeArg){

		int level = nodeArg.level;

		if(nodeArg.getType() == PlaylistNodeType.Internal){

			PlaylistNodeSecondaryIndex node = (PlaylistNodeSecondaryIndex) nodeArg;

			for(int j=0; j<level-1; j++)
					System.out.print("\t");
			System.out.println("<index>");

			for(int i=0; i<node.genreCount(); i++){
				for(int j=0; j<level-1; j++)
					System.out.print("\t");

				System.out.println(node.genreAtIndex(i));
			}

			
			for(int j=0; j<level-1; j++)
					System.out.print("\t");
			System.out.println("</index>");

			for(int i=0; i<node.getAllChildren().size(); i++)
				printSecondaryHelper(node.getChildrenAt(i));

		}
		else{
			PlaylistNodeSecondaryLeaf node = (PlaylistNodeSecondaryLeaf) nodeArg;

			for(int i=0; i<level-1; i++)
				System.out.print("\t");
			System.out.println("<data>");

			for(int i=0; i<node.genreCount(); i++){
				for(int j=0; j<level-1; j++)
					System.out.print("\t");

				System.out.println(node.genreAtIndex(i));
					
				for(int j=0; j<node.getSongBucket().get(i).size(); j++){
					for(int k=0; k<level; k++)
						System.out.print("\t");
					System.out.print("<record>");
					System.out.print(node.getSongBucket().get(i).get(j).fullName());
					System.out.println("</record>");
				}
			}
			
			for(int i=0; i<level-1; i++)
				System.out.print("\t");
			System.out.println("</data>");
		}

		return;
	}

}